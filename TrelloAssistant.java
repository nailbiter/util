package com.github.nailbiter.util;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.AssistantBotException;
import util.ParseCommentLine;

import static com.github.nailbiter.util.Util.HttpString;
import static com.github.nailbiter.util.Util.HTTPMETHOD;

public class TrelloAssistant {
	private static final String FIELDS = "name,due,dueComplete,id,labels,shortUrl,pos,email";
	String key_, token_;
	CloseableHttpClient client_ = HttpClients.createDefault();
	public TrelloAssistant(String key, String token) {
		key_ = key;
		token_ = token;
	}
	public JSONArray getCardsInList(String listid) throws Exception {
		System.err.println(String.format("id: %s", listid));
		String uri = 
				String.format(
						"https://api.trello.com/1/lists/%s/cards?%s",
						listid,
						JsonToUrl(new JSONObject()
								.put("key", key_)
								.put("token", token_)
								.put("fields", FIELDS)));
		String line = HttpString(uri,client_,true,HTTPMETHOD.GET);
		JSONArray res = new JSONArray(line);
		System.err.println(String.format("res.len = %d", res.length()));
		return res;
	}
	public JSONArray getAllCardsInList(String listid) throws Exception {
		System.err.println(String.format("id: %s", listid));
		String uri = 
				String.format(
						"https://api.trello.com/1/lists/%s/cards?%s",
						listid,
						JsonToUrl(new JSONObject()
								.put("key", key_)
								.put("filter", "all")
								.put("token", token_)
								.put("fields", FIELDS)));
		String line = HttpString(uri,client_,true,HTTPMETHOD.GET);
		JSONArray res = new JSONArray(line);
		System.err.println(String.format("res.len = %d", res.length()));
		return res;
	}
	public void setCardDuedone(String cardid,boolean duedone) throws Exception {
		String uri = String.format("https://api.trello.com/1/cards/%s?key=%s&token=%s&dueComplete=%s", 
				cardid,key_,token_,duedone?"true":"false"); 
		HttpString(uri,client_,false,HTTPMETHOD.PUT);
	}
	public String getCardEmail(String cardid) throws Exception{
		if(true)
			throw new AssistantBotException(AssistantBotException.Type.NOTYETIMPLEMENTED,
					"not yet implemented");
		String uri = null;
		String p = JsonToUrl(new JSONObject().put("key", key_).put("token", token_));
		
		HttpString(String.format("https://trello.com/1/cards/%s/markAssociatedNotificationsRead&%s", cardid,p),client_,true,HTTPMETHOD.POST);
			
		uri = String.format("https://trello.com/1/cards/%s?%s&actions=addAttachmentToCard,addChecklistToCard,addMemberToCard,commentCard,copyCommentCard,convertToCardFromCheckItem,createCard,copyCard,deleteAttachmentFromCard,emailCard,moveCardFromBoard,moveCardToBoard,removeChecklistFromCard,removeMemberFromCard,updateCard:idList,updateCard:closed,updateCard:due,updateCard:dueComplete,updateCheckItemStateOnCard,updateCustomFieldItem&actions_display=true&action_memberCreator_fields=fullName,initials,idEnterprise,idMemberReferrer,memberType,username,avatarHash,bio,bioData,confirmed,products,url,idPremOrgsAdmin&action_reactions=true&members=true&member_fields=fullName,initials,idEnterprise,idMemberReferrer,memberType,username,avatarHash,bio,bioData,confirmed,products,url,status&attachments=true&fields=email&checklists=all&checklist_fields=all&list=true&pluginData=true&customFieldItems=true&actions_limit=50",
				cardid,p);
		String line = HttpString(uri,client_,true,HTTPMETHOD.GET);
		JSONObject obj = new JSONObject(line);
		return obj.getString("email");
	}
	public void archiveCard(String cardid) throws Exception {
		String uri = String.format("https://api.trello.com/1/cards/%s?key=%s&token=%s&closed=true", cardid,key_,token_);
		HttpString(uri,client_,true,HTTPMETHOD.PUT);
	}
	public void setLabel(String cardid, String labelColor) throws Exception {
		System.err.println(String.format("cardid=%s, labelColor=%s", cardid,labelColor));
		String uri = 
				String.format("https://api.trello.com/1/cards/%s/labels?%s"
						,cardid
						,JsonToUrl(new JSONObject()
								.put("key", key_)
								.put("token", token_)
								.put("color", labelColor)
								.put("name", "failed")
								));
		HttpString(uri,client_,true,HTTPMETHOD.POST);
	}
	public void moveCard(String cardid, String newListId) throws Exception {
		moveCard(cardid,newListId,"bottom");
	}
	/**
	 * move with position specified
	 * @param cardid
	 * @param newListId if board is new, prefix with "BOARDID."
	 * @param pos "top" or "bottom"
	 * @throws Exception
	 */
	public void moveCard(String cardid, String newListId,String pos) throws Exception {
		if(!pos.equals("top") && !pos.equals("bottom") && !Pattern.matches("\\d+", pos))
			throw new Exception(String.format("uknown position %s", pos));
		System.err.println(String.format("cardid=%s, newListId=%s", cardid,newListId));
		JSONObject obj = new JSONObject()
				.put("key", key_)
				.put("token", token_)
				.put("pos", pos);
		if(newListId.contains(".")) {
			String[] split = newListId.split("\\.");
			System.err.format("split: \"%s\", \"%s\"\n",split[0],split[1]);
			obj.put("idBoard", split[0]);
			newListId = split[1];
		}
		obj.put("idList", newListId);
		String uri = String.format("https://api.trello.com/1/cards/%s?%s", cardid,JsonToUrl(obj));
		HttpString(uri,client_,true,HTTPMETHOD.PUT);
	}
	private static String JsonToUrl(JSONObject obj){
		ArrayList<String> args = new ArrayList<String>();
		for(String key:obj.keySet()) {
			args.add(String.format("%s=%s", key,obj.getString(key)));
		}
		return String.join("&", args);
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
	public void setLabelByName(String cardid,String labelname, String listid) throws JSONException, Exception {
		HashMap<String, JSONObject> labelsMap = 
				GetLabels(key_,token_,listid,client_);
		
//		curl --request POST \
//		  --url 'https://api.trello.com/1/cards/id/labels?color=color'
		String uri = String.format("https://api.trello.com/1/cards/%s/labels?%s" 
				,cardid
				,JsonToUrl(new JSONObject()
						.put("key", key_)
						.put("token", token_)
						.put("color", labelsMap.get(labelname).getString("color"))
						.put("name", labelname)
						));
		/*String reply = */HttpString(uri,client_,true,HTTPMETHOD.POST);
	}
	/**
	 * @param card 
	 * 	so far we support:
	 * 	name : String
	 * 	due : Date
	 * 	checklist : JSONArray
	 *  labelByName : JSONArray
	 */
	public JSONObject addCard(String idList,JSONObject card) throws Exception {
		SimpleDateFormat dateFormat = Util.GetTrelloDateFormat();
		JSONObject req = new JSONObject()
				.put("key", key_)
				.put("token", token_)
				.put("idList", idList)
				.put("name", URLEncoder.encode(card.getString("name")));
		if(card.has("due"))
			req.put("due", URLEncoder.encode(dateFormat.format(((Date)card.get("due")))));
		if( card.has("labelByName") ) {
			JSONArray labels = card.getJSONArray("labelByName");
			HashMap<String, JSONObject> labelsMap = 
					GetLabels(key_,token_,idList,client_);
			HashSet<String> labelIds = new HashSet<String>();
			for(Object o:labels) 
				labelIds.add(labelsMap.get((String)o).getString("id"));
			req.put("idLabels", 
					URLEncoder.encode(String.join(",", labelIds)));
		}
		String uri = String.format("https://api.trello.com/1/cards?%s", JsonToUrl(req));
		String reply = HttpString(uri,client_,true,HTTPMETHOD.POST);
		JSONObject res = new JSONObject(reply);
		if( card.has("checklist") ) {
			JSONArray checklist = card.getJSONArray("checklist");
			String checklistName = (String)checklist.remove(0);
			String id = res.getString("id");
			addCheckList(id,checklistName,checklist);
		}
		
		return res;
	}
	private static HashMap<String,JSONObject> GetLabels(String key, String token, String idList, CloseableHttpClient client) throws JSONException, Exception {
		HashMap<String, JSONObject> res = new HashMap<String,JSONObject>();
		String uri =  String.format("https://api.trello.com/1/boards/%s/labels?%s"
				,GetListsBoardId(key,token,idList,client)
				,JsonToUrl(new JSONObject()
						.put("key", key)
						.put("token", token)
						.put("fields", "id,name,color")));
		JSONArray labels = 
				new JSONArray(HttpString(uri,client,true,Util.HTTPMETHOD.GET));
		for(Object o:labels) {
			JSONObject obj = (JSONObject)o;
			res.put(obj.getString("name"), obj);
		}
		
		return res;
	}
	private static String GetListsBoardId(String key, String token, String idList, CloseableHttpClient client) throws JSONException, Exception {
		String uri =  String.format("https://api.trello.com/1/lists/%s?%s"
				,idList
				,JsonToUrl(new JSONObject()
						.put("key", key)
						.put("token", token)
						.put("fields", "idBoard")));
		JSONObject res = new JSONObject(HttpString(uri,client,true,Util.HTTPMETHOD.GET));
		return res.getString("idBoard");
	}
	void addCheckList(String cardId,String checklistName,JSONArray checklist) throws Exception {
		String uri = String.format("%s?key=%s&token=%s&idCard=%s&name=%s",
				"https://api.trello.com/1/checklists",
				key_,
				token_,
				cardId,
				URLEncoder.encode(checklistName)
		);
		String reply = HttpString(uri,client_,true,HTTPMETHOD.POST);
		JSONObject obj = new JSONObject(reply);
		System.err.format("got: %s\n", obj.toString(2));
		for(int i = 0; i < checklist.length(); i++) {
			String item = checklist.getString(i);
			addCheckListItem(obj.getString("id"),item);
		}
	}
	void addCheckListItem(String checkListId,String itemName) throws Exception {
		String uri = String.format("%s/%s/checkItems?key=%s&token=%s&name=%s&checked=false&pos=bottom",
				"https://api.trello.com/1/checklists/",
				checkListId,
				key_,
				token_,
				URLEncoder.encode(itemName)
		);
		String reply = HttpString(uri,client_,true,HTTPMETHOD.POST);
		System.err.format("%s=>\n%s\n", itemName,reply);
	}
	public void removeCard(String id) throws Exception {
		String uri = String.format("https://api.trello.com/1/cards/%s?key=%s&token=%s",id,key_,token_);
		HttpString(uri,client_,true,HTTPMETHOD.DELETE);
	}
	public void renameCard(String id, String newname) throws Exception {
		String uri = String.format("https://api.trello.com/1/cards/%s?%s",
				id,
				JsonToUrl(
						new JSONObject()
						.put("key", key_)
						.put("token", token_)
						.put("name", newname)
				)
			);
		HttpString(uri,client_,true,HTTPMETHOD.PUT);
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
