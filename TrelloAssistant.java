package com.github.nailbiter.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.github.nailbiter.util.Util.HttpString;
import static com.github.nailbiter.util.Util.HTTPMETHOD;

public class TrelloAssistant {
	String key_, token_;
	CloseableHttpClient client_ = HttpClients.createDefault();
	public TrelloAssistant(String key, String token) {
		key_ = key;
		token_ = token;
	}
	public JSONArray getCardsInList(String listid) throws Exception {
		System.out.println(String.format("id: %s", listid));
		String uri = 
				String.format(
						"https://api.trello.com/1/lists/%s/cards?key=%s&token=%s&fields=name,due,dueComplete,id", 
						listid,key_,token_);
		String line = HttpString(uri,client_,true,HTTPMETHOD.GET);
		JSONArray res = new JSONArray(line);
		System.out.println(String.format("res.len = %d", res.length()));
		return res;
	}
	public void setCardDuedone(String cardid,boolean duedone) throws Exception {
//		HttpPut put = new HttpPut(String.format("https://api.trello.com/1/cards/%s?key=%s&token=%s&dueComplete=%s", cardid,key_,token_,duedone?"true":"false"));
		String uri = String.format("https://api.trello.com/1/cards/%s?key=%s&token=%s&dueComplete=%s", 
				cardid,key_,token_,duedone?"true":"false"); 
		HttpString(uri,client_,false,HTTPMETHOD.PUT);
//		CloseableHttpResponse chr = client_.execute(put);
//		chr.close();
	}
	public void archiveCard(String cardid) throws Exception {
		String uri = String.format("https://api.trello.com/1/cards/%s?key=%s&token=%s&closed=true", cardid,key_,token_);
//        Util.PutString(uri,client_,true);
		HttpString(uri,client_,true,HTTPMETHOD.PUT);
	}
	public void setLabel(String cardid, String labelColor) throws Exception {
		System.out.println(String.format("cardid=%s, label=%s", cardid,labelColor));
		String uri = String.format("https://api.trello.com/1/cards/%s/labels?key=%s&token=%s&color=%s&name=failed", cardid,key_,token_,labelColor);
//        Util.PostString(uri,client_,true);
		HttpString(uri,client_,true,HTTPMETHOD.POST);
	}
	public void moveCard(String cardid, String newListId) throws Exception {
		System.out.println(String.format("cardid=%s, newListId=%s", cardid,newListId));
		String uri = String.format("https://api.trello.com/1/cards/%s?key=%s&token=%s&idList=%s", cardid,key_,token_,newListId);
//        Util.PutString(uri,client_,true);
		HttpString(uri,client_,true,HTTPMETHOD.PUT);
	}
	public String findListByName(String boardId,String listName) throws Exception {
		String uri = 
				String.format(
						"https://api.trello.com/1/boards/%s/lists?key=%s&token=%s&cards=none&fields=name", 
						boardId,key_,token_); 
		String r = HttpString(uri,client_,true,HTTPMETHOD.GET); 
		JSONArray res = new JSONArray(r);
		for(Object o:res) {
			JSONObject obj = (JSONObject)o;
			if(obj.getString("name").equals(listName))
				return obj.getString("id");
		}
		throw new Exception(String.format("no such list: %s", listName));
	}
	/*so far we support:
	 * 	name
	 * 	due
	 */
	public JSONObject addCard(String idList,JSONObject card) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String uri = String.format("https://api.trello.com/1/cards?key=%s&token=%s&idList=%s&name=%s%s", 
				key_,
				token_,
				idList,
				URLEncoder.encode(card.getString("name")),
				card.has("due")?("&due="+URLEncoder.encode(dateFormat.format(((Date)card.get("due"))))):"");
		String reply = HttpString(uri,client_,true,HTTPMETHOD.POST);
		return new JSONObject(reply);
	}
	public void removeCard(String id) throws Exception {
		//https://api.trello.com/1/cards/id
		String uri = String.format("https://api.trello.com/1/cards/%s?key=%s&token=%s",id,key_,token_);
//		Util.DeleteString(uri,client_,true);
		HttpString(uri,client_,true,HTTPMETHOD.DELETE);
	}
	public JSONArray getListActions(String listId, JSONObject obj) throws Exception {
		String uri = String.format("https://api.trello.com/1/lists/%s/actions?%s&fields=type,data,date",
				listId,keyTokenString());
		String res = HttpString(uri,client_,true,HTTPMETHOD.GET);
		return new JSONArray(res);
	}
	private String keyTokenString() {
		return String.format("key=%s&token=%s",key_,token_);
	}
}
