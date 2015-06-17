package com.dz.statuscheck;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
	public static Response parse(String rawResponse){
		Response res = null;
		if(rawResponse == null){
			return res;
		}
		res = new Response();
		Pattern patternTitle = Pattern.compile("<h1>(.*?)</h1>");
		Pattern patternDetail = Pattern.compile("<p>(.*?)</p>");
		Pattern patternType = Pattern.compile("your Form (.*?),");
		Matcher matcherTitle = patternTitle.matcher(rawResponse);
		Matcher matcherDetail = patternDetail.matcher(rawResponse);
		Matcher matcherType = patternType.matcher(rawResponse);
		if (matcherTitle.find()){
			res.setResTitle(matcherTitle.group(1));
		}
		if (matcherDetail.find()){
			res.setResDetail(matcherDetail.group(1));
		}
		if (matcherType.find()){
			res.setAppType(matcherType.group(1));
		}
		return res;
	}
}
