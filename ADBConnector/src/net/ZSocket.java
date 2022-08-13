package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZSocket {

	public String socketId = "DEFAULT";
	// 心跳包频率
	private static final double frame_heart = 1;
	private static ZSocket zSocket;
	private Sender zThread_send;
	private Sender zThread_sendFile;
	private Receiver zThread_recv;
	private Receiver zThread_recvFile;
	private ZThread zThread_heart;
	private int port = 16880;
	private int portFile = 16881;
	// 已连接的端点
	private ConcurrentHashMap<String, Client> clients;
	// 心跳超时时长
	private final int timeOut_heart = 3000;

	// 消息发送超时时长
	private long timeOut_send = 3000;
	// UDP每个包最大不能超过64kb
	public static final int maxPackagSize = 63 * 1024;

	// 缓存文件下载任务
	private Map<String, FileDownloadTask> fileDownloadTaskCache = new ConcurrentHashMap<>();
	// 缓存文件读取任务
	private Map<String, FileRandomReader> fileReaderTaskCache = new ConcurrentHashMap<>();
	// 缓存RPC回调,当一次完整的RPC请求完成后,从缓存去除
	private Map<String, AskResult> map_AskResult = new ConcurrentHashMap<>();
	// RPC应答
	private Map<String, Answer> map_Answer = new ConcurrentHashMap<>();
	private ReceiverResult receiver;
	private FilePathProcessor filePathProcessor;
	private boolean DEBUG;
	private String ip;
	private String cacheDir = "/zsocketcache/";

	// 发送的消息缓存
	private Map<String, Msg> cache_msg_send = new ConcurrentHashMap<>();

	private ZSocket() {
		clients = new ConcurrentHashMap<>();
		recv();
		recvFile();
		initSender();
		initFileSender();
		initHeart();
	}

	public ZSocket DEBUG() {
		this.DEBUG = true;
		return this;
	}

	public ZSocket setPort(int port) {
		if (S.isPort(port)) {
			this.port = port;
		} else {
			S.e("非法的端口号:" + port);
		}
		return this;
	}

	public void setTimeOut_send(long timeOut_send) {
		this.timeOut_send = timeOut_send;
	}

	public ZSocket setPortOfFileServer(int port) {
		if (S.isPort(port)) {
			this.portFile = port;
		} else {
			S.e("非法的FileServer端口号:" + port);
		}
		return this;
	}

	public ZSocket asServer() {
		socketId = "SERVER";
		return this;
	}

	public ZSocket asClient() {
		socketId = "CLIENT";
		return this;
	}

	public static ZSocket getInstance() {
		if (zSocket == null) {
			synchronized (ZSocket.class) {
				if (zSocket == null) {
					zSocket = new ZSocket();
					zSocket.ip = NetworkUtil.getIP();
				}
			}
		}
		return zSocket;
	}

	public void send(String msg, Msg.TimeOut timeOut) {
		send(msg, "", timeOut);
	}

	public void send(String msg, String ip, Msg.TimeOut timeOut) {
		send(msg.getBytes(), Msg.ASK, ip, timeOut);
	}

	public void send(byte[] msgData, byte type, String ip, Msg.TimeOut timeOut) {
		if (msgData == null) {
			return;
		}
		Msg msg = new Msg();
		msg.ip = ip;
		msg.type = type;
		msg.msg = msgData;
		msg.count = 1;
		msg.index = 0;
		send(msg, timeOut);
	}

	public synchronized void send(Msg msg, Msg.TimeOut timeOut) {
		msg.timeSend = S.currentTimeMillis();
		msg.timeOut = timeOut;
		cacheMsg(msg);
		zThread_send.send(msg);
	}

	public synchronized void sendFile(Msg msg) {
		msg.timeSend = S.currentTimeMillis();
		zThread_sendFile.send(msg);
	}

	public ZSocket cacheMsg(Msg msg) {
		cache_msg_send.put(msg.id, msg);
		return this;
	}

	public void ask(String request, AskResult askResult) {
		ask(request, null, askResult);
	}

	public void ask(String title, String params, final AskResult askResult) {
		String id = Msg.getRandomId();
		Msg msg = new Msg(id);
		msg.type = Msg.ASK;
		msg.title = title;
		if (S.isNotEmpty(params)) {
			msg.msg = params.getBytes();
		}
		send(msg, new Msg.TimeOut() {
			@Override
			public void whenTimeOut() {
				if (askResult != null) {
					askResult.whenTimeOut();
				}
			}
		});
		map_AskResult.put(id, askResult);
	}

	public ZSocket addAnswer(String ask, Answer answer) {
		map_Answer.put(ask, answer);
		return this;
	}

	public void setCacheDir(String cacheDir) {
		this.cacheDir = cacheDir;
	}

	private String getCacheDir() {
		String cacheDir = this.cacheDir.trim();
		return "~/Desktop/" + cacheDir;
	}

	public void downloadFile(final String filename, final FileDownloadResult fileDownloadResult) {
		if (S.isEmpty(filename)) {
			return;
		}
		final String id = Msg.getRandomId();
		final FileDownloadTask fileDownloadTask = new FileDownloadTask(id, getCacheDir(), filename, new FileDownloadTask.CallBack() {
			@Override
			public void whenFileNotFind(String filename) {
//                S.e("文件不存在:" + filename);
				if (fileDownloadResult != null) {
					fileDownloadResult.whenFileNotFind(filename);
				}
			}

			@Override
			public void whenDownloadCompleted(FileDownloadTask fileDownloadTask1, String filename) {
				fileDownloadTaskCache.remove(fileDownloadTask1.getTaskId());
//                S.s("文件下载完毕:" + filename);
				if (fileDownloadResult != null) {
					fileDownloadResult.whenDownloadCompleted(filename);
				}
			}

			@Override
			public void whenDownloading(String filename, float percent) {
//                S.s("正在下载文件[" + filename + "]:" + percent);
				if (fileDownloadResult != null) {
					fileDownloadResult.whenDownloading(filename, percent);
				}
			}

			@Override
			public void whenStartDownloading(String filename, long filesize) {
//                S.s("开始下载文件[" + filename + "] 大小:" + filesize);
				if (fileDownloadResult != null) {
					fileDownloadResult.whenStartDownloading(filename, filesize);
				}
			}

			@Override
			public void checkLost(final String filename, int[] lost, String ip, final FileDownloadTask fileDownloadTask) {
//                S.s("缺少文件块个数:" + lost.length + "=====================================================================>");
				if (lost.length <= 0) {
					return;
				}
				final Msg msg = new Msg(fileDownloadTask.getTaskId());
				msg.type = Msg.ASK_FILE_PIECE;
				msg.ip = ip;
				msg.filename = filename;
				byte[] pieceIndexs = new byte[lost.length * 4];
				for (int i = 0; i < lost.length; i++) {
					// 计算没有接收到的下标
					byte[] indexTmpArr = S.intToByteArr(lost[i]);
					System.arraycopy(indexTmpArr, 0, pieceIndexs, i * 4, indexTmpArr.length);
				}
				msg.msg = pieceIndexs;
				send(msg, new Msg.TimeOut() {
					@Override
					public void whenTimeOut() {
						if (fileDownloadTask != null) {
							fileDownloadTask.close();
						}
						fileDownloadTaskCache.remove(filename);
						if (fileDownloadResult != null) {
							fileDownloadResult.whenTimeOut();
						}
					}
				});
			}

		});
		// 缓存文件下载任务
		fileDownloadTaskCache.put(filename, fileDownloadTask);
		// 向服务器请求文件信息
		Msg msg = new Msg(id);
		msg.type = Msg.ASK_FILE_INFO;
		msg.filename = filename;
		S.s("正在请求文件信息:" + filename);
		send(msg, new Msg.TimeOut() {
			@Override
			public void whenTimeOut() {
				if (fileDownloadTask != null) {
					fileDownloadTask.close();
				}
				fileDownloadTaskCache.remove(filename);
				if (fileDownloadResult != null) {
					fileDownloadResult.whenTimeOut();
				}
			}
		});
	}

	private void initHeart() {
		stopHeart();
		zThread_heart = new ZThread(2) {
			DatagramSocket datagramSocket;
			long time_lastHeart;
			long duringMax;

			@Override
			protected void init() {
				// 计算心跳时间间隔
				duringMax = (long) (1000 / frame_heart);
				try {
					datagramSocket = new DatagramSocket();
				} catch (SocketException e2) {
					e2.printStackTrace();
					S.e(e2);
				}
			}

			@Override
			protected void todo() {
//                S.s("正在发送心跳:" + DETECTOR);
				long time_now = S.currentTimeMillis();
				long during_heart = time_now - time_lastHeart;
				if (during_heart > duringMax) {
					time_lastHeart = time_now;
					try {
						Msg msg = new Msg();
						msg.type = Msg.HEARTBIT;
						msg.msg = socketId.getBytes();
						byte[] data = Msg.getPackage(msg);
						DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), port);
						datagramSocket.send(datagramPacket);

						for (Client client : clients.values()) {
							long time = client.time_lastheart;
							long duringHeartTmp = time_now - time;
							// 大于超时时长未做出响应的client将被移除
							if (duringHeartTmp > timeOut_heart) {
								removeClient(client.ip);
							}
						}
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
						S.e(e1);
					} catch (IOException e) {
						e.printStackTrace();
						S.e(e);
					}
				}
				// 超时判断
				for (Msg msg : cache_msg_send.values()) {
					// 计算该消息从第一次创建到现在过了多久
					long during = time_now - msg.getTime();
					long during_lastSend = time_now - msg.timeSend;

					// 判断是否超时
					if (during > timeOut_send) {
						cache_msg_send.remove(msg.id);
//                        S.s("消息[" + msg.id + "]已超时,执行超时回调");
						if (msg.timeOut != null) {
							msg.timeOut.whenTimeOut();
						}
						return;
					} else {
						// 如果距离上一次发送小于1秒,该消息继续等待,发送下一个消息
						if (during_lastSend <= 1000) {
//                            S.s("消息[" + msg.id + "]正在等待回复");
						} else {
							// 如果距离上一次发送大于1秒小于3秒,该消息重新发送
//                            S.s("消息[" + msg.id + "]需要发送,正在发送");
							if (zThread_send != null) {
								zThread_send.send(msg);
							}
						}
					}
				}
			}

			@Override
			public void close() {
				super.close();
				try {
					if (datagramSocket != null) {
						datagramSocket.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		zThread_heart.start();
	}

	private void initSender() {
		zThread_send = new Sender(port, clients, null);
		zThread_send.start();
	}

	private void initFileSender() {
		zThread_sendFile = new Sender(portFile, clients, new Sender.CallBack() {
			@Override
			public void whenSend() {
			}
		});
		zThread_sendFile.start();
	}

	private void recv() {
		zThread_recv = new Receiver(port, new Receiver.CallBack() {
			@Override
			public void whenGotMsg(final Msg msg) {
				final String id = msg.id;

				final String ip = msg.ip;
				final byte type = msg.type;
				switch (type) {
				case Msg.HEARTBIT:
					Client client = getClient(ip);
					String socketIdTmp = new String(msg.msg);
					// 如果是不同类型的socket,则加入连接
					if (!socketIdTmp.equals(socketId) || DEBUG) {
						if (client == null) {
							client = new Client();
							client.ip = ip;
							client.port = port;
							// 保存远程ip
							addClient(client);
						}
						// 刷新最新一次通信的时间
						client.time_lastheart = S.currentTimeMillis();
					}
					break;
				case Msg.LOGOUT:
					removeClient(ip);
					break;
				case Msg.ASK:
					S.s("接到来自[" + ip + ":" + port + "]的请求 : " + msg.title);
					Answer answer = map_Answer.get(msg.title);
					// 获取param
					String param = new String(msg.msg);
					if (answer != null) {
						String answerStr = answer.getAnswer(param);
//                            S.s("正在回复:" + answerStr);
						Msg response = new Msg(id);
						response.type = Msg.ANSWER;
						if (S.isNotEmpty(answerStr)) {
							response.msg = answerStr.getBytes();
						}
						response.ip = msg.ip;
						send(response, null);
					} else {
						if (receiver != null) {
							receiver.whenGotResult(param);
						}
					}
					break;
				case Msg.ANSWER:
					String stringMsg2 = new String(msg.msg);
					S.s("接到来自[" + ip + ":" + port + "]的回复 : " + stringMsg2);
					AskResult askResult_rpc = map_AskResult.remove(msg.id);
					if (askResult_rpc != null) {
						askResult_rpc.whenGotResult(msg);
					} else {
						if (receiver != null) {
							receiver.whenGotResult(stringMsg2);
						}
					}
					break;
				case Msg.ASK_FILE_INFO:// 文件信息请求
					S.s("接到文件信息请求:" + msg.filename);
					String path = "~/Desktop/";
					if (filePathProcessor != null) {
						path = filePathProcessor.getFilePath(msg.filename);
					}
					// 预加载文件读取器
					FileRandomReader fileRandomReader = new FileRandomReader(msg.id, path, msg.filename, new FileRandomReader.CallBack() {
						@Override
						public void whenTimeOut(String filename) {
//                                S.s("文件读取超时,移除读取线程");
							fileReaderTaskCache.remove(filename);
						}

						@Override
						public void whenFileNotFind(String filename) {
							S.e("FileRandomReader:文件[" + filename + "]不存在,无法获取文件信息");
							fileReaderTaskCache.remove(filename);
							// 回复文件信息
							Msg response = new Msg();
							response.type = Msg.FILE_INFO;
							response.filename = filename;
							response.ip = msg.ip;
//                                S.s("文件未找到,正在回复至[" + msg.ip + "]");
							send(response, new Msg.TimeOut() {
								@Override
								public void whenTimeOut() {
									S.e("TimeOut:已发送文件未找到的信息,但未收到客户端的回复");
								}
							});
						}

						@Override
						public void whenFileFind(String filename, FileRandomReader fileRandomReader) {
//                                S.s("文件[" + filename + "]存在,预加载成功,读取器加入缓存");
							fileReaderTaskCache.put(filename, fileRandomReader);
						}

						@Override
						public void whenGotFileInfo(int count, int piecesize, int filesize) {
							S.s("文件信息已获取,文件块数量:" + count + " 文件块大小:" + piecesize + " 文件总大小:" + filesize);
							// 文件块大小
							byte[] pieceSizeArr = S.intToByteArr(piecesize);
							// 回复文件信息
							Msg response = new Msg();
							response.type = Msg.FILE_INFO;
							response.msg = pieceSizeArr;
							response.count = count;
							response.filename = msg.filename;
							response.filesize = filesize;
							response.ip = msg.ip;
//                                S.s("正在回复文件信息到[" + msg.ip + "]");
							send(response, new Msg.TimeOut() {
								@Override
								public void whenTimeOut() {
									S.e("TimeOut:已回复文件信息,但未收到客户端响应");
								}
							});
						}
					});
					fileRandomReader.setPieceSize(maxPackagSize);
					fileRandomReader.start();
					break;
				case Msg.FILE_INFO:
					// 获取文件下载任务,获取文件信息
					FileDownloadTask fileDownloadTask = fileDownloadTaskCache.get(msg.filename);
					if (fileDownloadTask != null) {
						// 文件块大小
						int pieceSize = S.byteArrToInt(msg.msg);
						fileDownloadTask.setPieceSize(pieceSize);
						fileDownloadTask.setFileSize(msg.filesize);
						fileDownloadTask.setCount(msg.count);
						fileDownloadTask.setIp(msg.ip);
						fileDownloadTask.start();
					}
					break;
				case Msg.ASK_FILE_PIECE:
					// 向请求者发送文件块
					final FileRandomReader reader = fileReaderTaskCache.get(msg.filename);
					if (reader != null) {
						reader.getFilePiece(new FileRandomReader.Reader() {

							@Override
							public int[] getIndexs() {
								byte[] indexsByteArr = msg.msg;
								int[] indexs = new int[0];
								// byte数组转化成int数组
								if (indexsByteArr != null && indexsByteArr.length > 0 && indexsByteArr.length % 4 == 0) {
									for (int i = 0; i < indexsByteArr.length; i += 4) {
										int index = S.byteArrToInt(Arrays.copyOfRange(indexsByteArr, i, i + 4));
										indexs = Arrays.copyOf(indexs, indexs.length + 1);
										indexs[indexs.length - 1] = index;
									}
								}
								return indexs;
							}

							@Override
							public void whenGotFilePiece(String filename, int count, final int index, byte[] piece) {
								Msg response = new Msg();
								response.filename = msg.filename;
								response.count = count;
								response.index = index;
								response.msg = piece;
								response.type = Msg.FILE_PIECE;
								response.ip = msg.ip;
								sendFile(response);
							}

							@Override
							public void whenTaskEnd() {
								Msg response = new Msg();
								response.filename = msg.filename;
								response.type = Msg.FILE_CHECK;
								response.ip = msg.ip;
								send(response, new Msg.TimeOut() {
									@Override
									public void whenTimeOut() {
										S.e("FILE_CHECK:未接收到客户端的回复");
									}
								});
							}
						});
					}
					break;
				case Msg.FILE_CHECK:
//                        S.s("==> 本次任务完成,检查丢包");
					// 获取文件下载任务,获取文件信息
					FileDownloadTask fileDownloadTask_check = fileDownloadTaskCache.get(msg.filename);
					if (fileDownloadTask_check != null) {
						fileDownloadTask_check.check();
					}
					break;
				default:
					S.s("接到来自[" + ip + ":" + port + "]" + "(type:" + msg.type + ")的未知类型消息");
					break;
				}
			}

			@Override
			public void whenGotResponse(String id) {
				removeMsg(id);
			}

			@Override
			public Sender getSender() {
				return zThread_send;
			}

			@Override
			public String getIp() {
				return ZSocket.this.ip;
			}
		});
		zThread_recv.start();
	}

	public void removeMsg(String id) {
		cache_msg_send.remove(id);
//        S.s("消息[" + id + "]已收到回复,移除");
	}

	private void recvFile() {
		zThread_recvFile = new Receiver(portFile, new Receiver.CallBack() {
			@Override
			public void whenGotMsg(Msg msg) {
				String ip = msg.ip;
				switch (msg.type) {
				case Msg.FILE_PIECE:
//                        S.s("接到文件块----- " + msg.index);
					// 获取文件下载任务,插入文件块
					FileDownloadTask fileDownloadTask_assemble = fileDownloadTaskCache.get(msg.filename);
					if (fileDownloadTask_assemble != null) {
						fileDownloadTask_assemble.addPiece(msg.msg, msg.index);
					}
					break;
				default:
					S.s("接到来自[" + ip + "]" + "(type:" + msg.type + ")的未知类型消息");
					break;
				}
			}

			@Override
			public void whenGotResponse(String id) {
				removeMsg(id);
			}

			@Override
			public Sender getSender() {
				return zThread_sendFile;
			}

			@Override
			public String getIp() {
				return ZSocket.this.ip;
			}
		});
		zThread_recvFile.start();
	}

	public ZSocket setReceiver(ReceiverResult receiver) {
		this.receiver = receiver;
		return this;
	}

	public void clearClient() {
		synchronized (ZSocket.class) {
			clients.clear();
		}
	}

	public void addClient(Client client) {
		synchronized (ZSocket.class) {
			if (S.isIp(client.ip)) {
				S.s("[" + client.ip + "]已连接");
				clients.put(client.ip, client);
			}
		}
		resumeSender();
	}

	private void resumeSender() {
		if (zThread_send != null) {
			zThread_send.resumeThread();
		}
		if (zThread_sendFile != null) {
			zThread_sendFile.resumeThread();
		}
	}

	public Client getClient(String ip) {
		synchronized (ZSocket.class) {
			return clients.get(ip);
		}
	}

	public void removeClient(String ip) {
		synchronized (ZSocket.class) {
			S.e("[" + ip + "]已断开连接");
			clients.remove(ip);
		}
	}

	public void stopSend() {
		if (zThread_send != null) {
			zThread_send.close();
			zThread_send = null;
		}
		if (zThread_sendFile != null) {
			zThread_sendFile.close();
			zThread_sendFile = null;
		}
	}

	public void stopRecv() {
		if (zThread_recv != null) {
			zThread_recv.close();
			zThread_recv = null;
		}
		if (zThread_recvFile != null) {
			zThread_recvFile.close();
			zThread_recvFile = null;
		}
	}

	public void clearTask() {

		for (FileDownloadTask fileDownloadTask : fileDownloadTaskCache.values()) {
			fileDownloadTask.close();
		}
		for (FileRandomReader fileRandomReader : fileReaderTaskCache.values()) {
			fileRandomReader.close();
		}
		fileDownloadTaskCache.clear();
		fileReaderTaskCache.clear();
		map_Answer.clear();
		map_AskResult.clear();
		cache_msg_send.clear();
	}

	private void stopHeart() {
		if (zThread_heart != null) {
			zThread_heart.close();
			zThread_heart = null;
		}
	}

	public static void close() {
		if (zSocket != null) {
			zSocket.stopSend();
			zSocket.stopRecv();
			zSocket.stopHeart();
			zSocket.clearTask();
			zSocket.clearClient();
			zSocket = null;
		}
	}

	public ZSocket setFilePathProcessor(FilePathProcessor filePathProcessor) {
		this.filePathProcessor = filePathProcessor;
		return this;
	}

	public interface CallBack {
		void whenTimeOut();
	}

	public interface AskResult extends CallBack {
		void whenGotResult(Msg msg);
	}

	public interface Answer {
		String getAnswer(String param);
	}

	public interface FilePathProcessor {
		String getFilePath(String filename);
	}

	public interface ReceiverResult extends CallBack {
		void whenGotResult(String msg);
	}

	public interface FileDownloadResult extends CallBack {

		void whenFileNotFind(String filename);

		void whenDownloadCompleted(String filename);

		void whenDownloading(String filename, float percent);

		void whenStartDownloading(String filename, long filesize);
	}

}
