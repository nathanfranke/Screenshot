package net.nathanfranke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import net.dv8tion.Uploader;

public class ImageDeleter {
	
	public static void delete (String deleteHash) {
		System.out.println("Deleting image with hash: " + deleteHash + ", Result: " + Uploader.delete(deleteHash));
	}
	
	public static void addHashForFutureDeletion (String deleteHash) {
		Config.set("hashes", Config.get("hashes") + deleteHash + ":" + new Date().getTime() + "\n");
	}
	
	public static void checkDeletion (boolean deleteAll) {
		int secondsToDelete = Config.getInt("deleteTime", 3600);
		if(secondsToDelete <= 0) {
			return;
		}
		ArrayList<String> entries = new ArrayList<String>();
		for (String s : Config.get("hashes").split("\n")) entries.add(s);
		ArrayList<Integer> removeEntries = new ArrayList<Integer>();
		int current = 0;
		for (String s : entries) {
			current++;
			if(!s.isEmpty()) {
				String[] split = s.split(":");
				if(split.length == 2) {
					String hash = split[0];
					Date then = new Date(Long.parseLong(split[1]));
					long difference = new Date().getTime()-then.getTime();
					if(((int)(difference/1000)) > secondsToDelete || deleteAll) {
						delete(hash);
						removeEntries.add(current-1);
					}
				}
			}
		}
		Collections.reverse(removeEntries);
		for (Integer i : removeEntries) {
			entries.remove((int)i);
		}
		Config.set("hashes", String.join("\n", entries));
	}
	
}
