package org.nirland.websocket;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Helper utility class which implements some useful functions.
 * 
 * @author Nirland
 */

public class Utils {

	public final static int MAX_NAME_LENGTH = 15;
	public final static int MAX_CHATMESSAGE_LENGTH = 150;

	public static String escapeHTML(String str) {
		String[] htmlChars = new String[] { "&", "\"", "<", ">" };
		String[] replacement = new String[] { "&amp;", "&quot;", "&lt;", "&gt;" };

		for (int i = 0; i < htmlChars.length; i++) {
			str = str.replace(htmlChars[i], replacement[i]);
		}
		return str;
	}

	public static String checkName(String rawName) {
		rawName = rawName.replaceAll("[^A-Za-zР-пр-џ0-9]", "");
		if (rawName.length() > MAX_NAME_LENGTH){
			return rawName.substring(0, MAX_NAME_LENGTH - 1);
		} else{
			return rawName;
		}		
	}

	public static String submessage(String message) {
		if (message.length() > MAX_CHATMESSAGE_LENGTH){
			return message.substring(0, MAX_CHATMESSAGE_LENGTH - 1);
		} else{
			return message;
		}		
	}

	public static <T> T randomPoll(List<? extends T> list) {
		Random rand = new Random();
		int index = rand.nextInt(list.size());
		T elem = list.get(index);
		list.remove(index);
		return elem;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map, final boolean desc) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				int direction = desc ? -1 : 1;
				return (o1.getValue()).compareTo(o2.getValue()) * direction;
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static JSONArray packResults(Map<SocketConnection, Integer> results) {
		JSONArray jsonArr = new JSONArray();
		for (SocketConnection socket : results.keySet()) {
			User user = socket.getUser();
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("uid", user.getId());
			jsonObj.put("uname", user.getName());
			jsonObj.put("points", results.get(socket));
			jsonArr.add(jsonObj);
		}
		return jsonArr;
	}
}
