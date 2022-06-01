package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.OpenApiType;
import kr.finpo.api.domain.Policy;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.xml.bind.annotation.*;


@Getter
@ToString
@XmlRootElement(name = "empsInfo")
@XmlAccessorType(XmlAccessType.NONE)
public class YouthcenterDto {

  @XmlAttribute(name = "totalCnt")
  private String totalCnt;

  @XmlElement(name = "emp")
  private Emp[] emp;


  @Getter
  @ToString
  @XmlRootElement(name = "emp")
  public static class Emp {
    @XmlElement(name = "bizId")
    String bizId;
    @XmlElement(name = "polyBizSecd")
    String polyBizSecd;
    @XmlElement(name = "polyBizTy")
    String polyBizTy;
    @XmlElement(name = "polyBizSjnm")
    String polyBizSjnm;
    @XmlElement(name = "polyItcnCn")
    String polyItcnCn;
    @XmlElement(name = "plcyTpNm")
    String plcyTpNm;
    @XmlElement(name = "sporScvl")
    String sporScvl;
    @XmlElement(name = "sporCn")
    String sporCn;
    @XmlElement(name = "ageInfo")
    String ageInfo;
    @XmlElement(name = "empmSttsCn")
    String empmSttsCn;
    @XmlElement(name = "accrRqisCn")
    String accrRqisCn;
    @XmlElement(name = "majrRqisCn")
    String majrRqisCn;
    @XmlElement(name = "splzRlmRqisCn")
    String splzRlmRqisCn;
    @XmlElement(name = "cnsgNmor")
    String cnsgNmor;
    @XmlElement(name = "rqutPrdCn")
    String rqutPrdCn;
    @XmlElement(name = "rqutProcCn")
    String rqutProcCn;
    @XmlElement(name = "jdgnPresCn")
    String jdgnPresCn;
    @XmlElement(name = "rqutUrla")
    String rqutUrla;

    public Policy toEntity() {
      return Policy.of(
          polyBizSjnm.trim()
          , bizId.trim()
          , (cnsgNmor.trim().equals("-") || cnsgNmor.trim().contains("null")) ? "" : cnsgNmor.trim().equals("주관기관과 동일") ? polyBizTy.trim() : cnsgNmor.trim()
          , (polyItcnCn.trim().equals("-") || polyItcnCn.trim().contains("null")) ? "" : polyItcnCn.trim()
          , (sporScvl.trim().equals("-") || sporScvl.trim().contains("null")) ? "" : sporScvl.trim()
          , (sporCn.trim().equals("-") || sporCn.trim().contains("null")) ? "" : sporCn.trim()
          , (ageInfo.trim().equals("-") || ageInfo.trim().contains("null")) ? "" : ageInfo.trim()
          , (rqutPrdCn.trim().equals("-") || rqutPrdCn.trim().contains("null")) ? "" : rqutPrdCn.trim()
          , null
          , null
          , (rqutProcCn.trim().equals("-") || rqutProcCn.trim().contains("null")) ? "" : rqutProcCn.trim()
          , (jdgnPresCn.trim().equals("-") || jdgnPresCn.trim().contains("null")) ? "" : jdgnPresCn.trim()
          , (rqutUrla.trim().equals("-") || rqutUrla.trim().contains("null") || rqutUrla.trim().contains("없음") || rqutUrla.trim().contains("미정")) ? "" : rqutUrla.trim()
          , OpenApiType.YOUTHCENTER);
    }
  }

}
