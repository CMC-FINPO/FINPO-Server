package kr.finpo.api.constant;

import kr.finpo.api.exception.GeneralException;

import java.util.*;

public class RegionConstant {

  public static final List<String> seoul = Arrays.asList("종로", "중구", "용산", "성동", "광진", "동대문", "중랑", "성북", "강북", "도봉", "노원", "은평", "서대문", "마포", "양천", "강서", "구로", "금천", "영등포", "동작", "관악", "서초" , "강남", "송파", "강동");

  public static final List<String> busan = Arrays.asList("중구", "서구", "동구", "영도", "부산진", "동래구", "남구", "북구", "해운대", "사하", "금정", "강서", "연제", "수영", "사상");

  public static final List<String> gyeonggi = Arrays.asList("수원", "성남", "의정부", "안양", "부천", "광명", "평택", "동두천", "안산", "고양", "과천", "구리", "남양주", "오산", "시흥", "군포", "의왕", "하남", "용인", "파주", "이천", "안성", "김포", "화성", "광주", "양주", "포천", "여주");

  public static final List<String> regions1 = Arrays.asList("서울", "경기", "부산");
  public static List<List> regions2 = Arrays.asList(seoul, gyeonggi, busan);

  public static final Long REGION2_MAX = 100L;


  public static Long getKey(String region1, String region2) {
    try {
      int region1Idx = regions1.indexOf(region1);
      return Long.valueOf(region1Idx * REGION2_MAX + (region2.isEmpty() ? 0 : (regions2.get(region1Idx).indexOf(region2)) + 1));
    }
    catch(Exception e) {
      throw new GeneralException(ErrorCode.BAD_REQUEST, "Region name not valid");
    }
  }

  public static String getRegion1(Long key) {
    try {
      return regions1.get((int) (key / REGION2_MAX));
    }
    catch(Exception e) {
      throw new GeneralException(ErrorCode.BAD_REQUEST, "Region key not valid");
    }
  }

  public static String getRegion2(Long key) {
    try {
      if(key % REGION2_MAX == 0) return "";
      return (String) regions2.get((int) (key / REGION2_MAX)).get((int) (key % REGION2_MAX - 1));
    }
    catch(Exception e) {
      throw new GeneralException(ErrorCode.BAD_REQUEST, "Region key not valid");
    }
  }

  public static List getRegions2(String region1) {
    try {
      return regions2.get((int) regions1.indexOf(region1));
    }
    catch(Exception e) {
      throw new GeneralException(ErrorCode.BAD_REQUEST, "Region key not valid");
    }
  }
}