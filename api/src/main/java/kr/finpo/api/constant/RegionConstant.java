package kr.finpo.api.constant;

import java.util.HashMap;

public class RegionConstant {

  public static final String[] seoul = {"종로", "중구", "용산", "성동", "광진", "동대문", "중랑", "성북", "강북", "도봉", "노원", "은평", "서대문", "마포", "양천", "강서", "구로", "금천", "영등포", "동작", "관악", "서초", "강남", "송파", "강동"};

  public static final String[] busan = {"중구", "서구", "동구", "영도", "부산진", "동래구", "남구", "북구", "해운대", "사하", "금정", "강서", "연제", "수영", "사상"};

  public static final String[] gyeonggi = {"수원", "성남", "의정부", "안양", "부천", "광명", "평택", "동두천", "안산", "고양", "과천", "구리", "남양주", "오산", "시흥", "군포", "의왕", "하남", "용인", "파주", "이천", "안성", "김포", "화성", "광주", "양주", "포천", "여주"};

  public static final String[] regions1 = {"서울", "경기", "부산"};

  public static HashMap<String, String[]> regions2 = new HashMap<>() {{
    put("서울", seoul);
    put("경기", gyeonggi);
    put("부산", busan);
  }};

}