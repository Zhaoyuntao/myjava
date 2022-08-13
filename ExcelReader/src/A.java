import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class A {
	public static String divider(String phoneNumber, String dividerString, int[] divider) {
		if (phoneNumber == null || phoneNumber.trim() == "" || divider == null || divider.length == 0) {
			return phoneNumber;
		}
		phoneNumber = phoneNumber.trim();
		StringBuilder stringBuilder = new StringBuilder();
		int length = 0;
		for (int i = 0; i < divider.length; i++) {

			int start = length;
			int end = length + divider[i];
			if (start > phoneNumber.length()-1) {
				break;
			} else if (end >= phoneNumber.length()) {
				stringBuilder.append(phoneNumber.substring(start));
				break;
			} else {
				if (i < divider.length - 1) {
					stringBuilder.append(phoneNumber.substring(start, end)).append(dividerString);
				} else {
					stringBuilder.append(phoneNumber.substring(start, end)).append(dividerString);
					stringBuilder.append(phoneNumber.substring(end));
				}
			}

			length += divider[i];
			System.out.println(length);
		}

		return stringBuilder.toString();
	}

	public static void main(String[] args) {
		try {
			System.out.println(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
