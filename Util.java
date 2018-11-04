package com.github.nailbiter.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Util {
	public static enum HTTPMETHOD{
		GET,POST,DELETE,PUT
	};
	public static String HttpString(String url,CloseableHttpClient client_,boolean verbose,HTTPMETHOD method) throws Exception {
		if(method==HTTPMETHOD.GET) {
			return GetString(url,client_);
		} else if(method==HTTPMETHOD.DELETE) {
			DeleteString(url,client_,verbose);
			return null;
		} else if(method==HTTPMETHOD.POST) {
			return PostString(url,client_,verbose);
		} else if(method==HTTPMETHOD.PUT) {
			PutString(url,client_,verbose);
			return null;
		} else {
			throw new Exception("method not recognized");
		}
	}
	private static String GetString(String url,CloseableHttpClient client_) throws ClientProtocolException, IOException {
		System.out.println(String.format("%s method for url: %s","get", url));
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse chr = client_.execute(get);
		BufferedReader br = new BufferedReader(new InputStreamReader(chr.getEntity().getContent()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
	    }
		System.out.println(String.format("res: %s",sb.toString()));
		chr.close();
		return sb.toString();
	}

	private static void DeleteString(String uri,CloseableHttpClient client_,boolean verbose) throws ClientProtocolException, IOException {
		System.out.println(String.format("uri in DeleteString: %s", uri));
		HttpDelete put = new HttpDelete(uri);
		if(!verbose) {
			CloseableHttpResponse chr = client_.execute(put);
			chr.close();
		}else{
			CloseableHttpResponse chr = client_.execute(put); 
			BufferedReader br = new BufferedReader(new InputStreamReader(chr.getEntity().getContent()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
		    }
			System.out.println(String.format("reply: %s", sb.toString()));
			chr.close();
		}
	}

	private static String PostString(String uri,CloseableHttpClient client_,boolean verbose) throws ClientProtocolException, IOException {
		System.out.println(String.format("uri: %s", uri));
		HttpPost put = new HttpPost(uri);
		if(!verbose) {
			CloseableHttpResponse chr = client_.execute(put);
			chr.close();
			return null;
		}else{
			CloseableHttpResponse chr = client_.execute(put); 
			BufferedReader br = new BufferedReader(new InputStreamReader(chr.getEntity().getContent()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
		    }
			System.out.println(String.format("reply: %s", sb.toString()));
			chr.close();
			return sb.toString();
		}
	}

	private static void PutString(String uri,CloseableHttpClient client_,boolean verbose) throws ClientProtocolException, IOException {
		System.out.println(String.format("uri: %s", uri));
		HttpPut put = new HttpPut(uri);
		if(!verbose) {
			CloseableHttpResponse chr = client_.execute(put);
			chr.close();
		}else{
			CloseableHttpResponse chr = client_.execute(put); 
			BufferedReader br = new BufferedReader(new InputStreamReader(chr.getEntity().getContent()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
		    }
			System.out.println(String.format("reply: %s", sb.toString()));
			chr.close();
		}
	}
	public static JSONObject getJSONObject(String fname) {
		FileReader fr = null;
		JSONObject res = null;
		try {
			System.out.println("storageManager gonna open: "+fname);
			fr = new FileReader(fname);
			StringBuilder sb = new StringBuilder();
	        int character;
	        while ((character = fr.read()) != -1) {
	        		sb.append((char)character);
	        }
	        System.out.println("found "+sb.toString());
			fr.close();
			res = (JSONObject) (new JSONTokener(sb.toString())).nextValue();
		}
		catch(Exception e) {
			System.out.println("found nothing");
			res = new JSONObject();
		}
		return res;
	}

}
