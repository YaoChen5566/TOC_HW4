/************************************************************

資訊104乙 F74006242 郭曜禎

執行範例:
		java -jar TocHw4.jar http://www.datagarage.io/api/5385b69de7259bb37d925971
輸出範例:
		臺中市南區忠明南路, 最高成交價: 9500000, 最低成交價: 3960000

		臺中市后里區墩北里大興路, 最高成交價: 10000000, 最低成交價: 9380000

程式敘述:利用inputstreamReader和URL將資料擷取下來，將輸入的argument轉變為Pattern做Regular Expression
	       和JSON格式裡的內容作比較，當讀取到路名時，則和該筆交易日期加起來為一個String，存到set裡，判斷set中是否有重複
	      沒有重複者加入set，並在Map中的技術次數+1，最後將Map利用collection的sort排序找出最大值，再找出最大值的Key
	      當作Pattern來對照並找出相對應資料中的交易價格的最大及最小值。
	       
************************************************************/

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import org.json.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class TocHw4 {

	public static void main(String[] args) {
		
		
		
		if(args.length!=1)
		{
			System.out.println("Arguments error!");
			return;
		}
		
		String WebAddress = args[0];
		Pattern MainRoad = Pattern.compile(".*(路|街|大道)");
		Pattern Alley = Pattern.compile(".*巷");
		
		HashMap<String,Integer> Count = new HashMap<String, Integer>();
		HashSet RoadAndDate = new HashSet();
		Matcher MatchMainRoad;
		Matcher MatchAlley;
		Matcher Main;
		String tmpData;
		boolean Exist = false;
		
		int CurrentMaxindex = 0;
		Integer tmp;
		try {
			
			InputStreamReader input = new InputStreamReader(new URL(WebAddress).openStream(),"UTF-8");
			
			JSONArray ParseData = new JSONArray(new JSONTokener(input));
			
			for(int i = 0 ; i < ParseData.length() ; i++)
			{
				JSONObject BigData = ParseData.getJSONObject(i);
				
				MatchMainRoad = MainRoad.matcher(BigData.get("土地區段位置或建物區門牌").toString());
				MatchAlley = Alley.matcher(BigData.get("土地區段位置或建物區門牌").toString());
				if(MatchMainRoad.find())
				{
					tmpData = MatchMainRoad.group()+(BigData.get("交易年月").toString());
					if(!RoadAndDate.contains(tmpData))
					{
						RoadAndDate.add(tmpData);
						if(Count.containsKey(MatchMainRoad.group()))
						{
							tmp = Count.get(MatchMainRoad.group());
							tmp++;
							Count.put(MatchMainRoad.group(),tmp);
						}
						else
						{
							Count.put(MatchMainRoad.group(),1);
						}
					}
					
				}
				else if(!MatchMainRoad.find() && MatchAlley.find())
				{
					tmpData = MatchAlley.group()+(BigData.get("總價元").toString());
					if(!RoadAndDate.contains(tmpData))
					{
						RoadAndDate.add(tmpData);
						if(Count.containsKey(MatchAlley.group()))
						{
							tmp = Count.get(MatchAlley.group());
							tmp++;
							Count.put(MatchAlley.group(),tmp);
						}
						else
						{
							Count.put(MatchAlley.group(),1);
						}
					}
				}
				
			}
			
			List<Map.Entry<String, Integer>> BeSorted = new ArrayList<Map.Entry<String,Integer>> (Count.entrySet());
			
			Collections.sort(BeSorted, new Comparator<Map.Entry<String,Integer>>()
			{
				public int compare(Map.Entry<String,Integer> o1,Map.Entry<String,Integer> o2)
				{
					return (o2.getValue()-o1.getValue());
				}
			});
			
			
			ArrayList<String> MaxRoad = new ArrayList<String>();
			MaxRoad.add(BeSorted.get(0).getKey());
		
			for(int i = 1 ; i < BeSorted.size() ; i++)
			{
				if(BeSorted.get(0).getValue() == BeSorted.get(i).getValue())
				{
					MaxRoad.add(BeSorted.get(i).getKey());
				}
			}
			
			int MaxSell = 0;
			int MinSell = 0;
			boolean First = true;
			
			Pattern WantedRoad;
			for(int h = 0 ; h < MaxRoad.size() ; h++)
			{
				WantedRoad = Pattern.compile(".*"+MaxRoad.get(h));
				for(int i = 0 ; i < ParseData.length() ; i++)
				{
					JSONObject BigData = ParseData.getJSONObject(i);
					Main = WantedRoad.matcher(BigData.get("土地區段位置或建物區門牌").toString());
					if(Main.find())
					{
						if(First)
						{
							MaxSell = Integer.valueOf(BigData.get("總價元").toString());
							MinSell = Integer.valueOf(BigData.get("總價元").toString());
							First = false;
						}
						else
						{
							if(Integer.valueOf(BigData.get("總價元").toString()) > MaxSell)
							{
								MaxSell = Integer.valueOf(BigData.get("總價元").toString());	
							}
							
							if(Integer.valueOf(BigData.get("總價元").toString()) < MinSell)
							{
								MinSell = Integer.valueOf(BigData.get("總價元").toString());
							}
						}
					}
				}
				
				System.out.println(MaxRoad.get(h)+", 最高成交價: " + MaxSell + ", 最低成交價: " + MinSell + "\n");
				
				First = true;
			}
			
		} 
		catch (IOException e)
		{
			System.out.println("IOException");
		} catch (JSONException e) {
			System.out.println("JSONException");
		}
	}

}
