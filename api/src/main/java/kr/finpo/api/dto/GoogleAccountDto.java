package kr.finpo.api.dto;

import kr.finpo.api.constant.Gender;
import kr.finpo.api.constant.OAuthType;
import lombok.Getter;
import org.joda.time.format.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public record GoogleAccountDto(
    String id,
    String resourceName,
    String etag,
    ArrayList<Gender> genders,
    ArrayList<Birthday> birthdays,
    ArrayList<Name> names,
    ArrayList<EmailAddress> emailAddresses,
    ArrayList<Photo> photos
) {

  record Gender(
      String value
  ) {
  }

  record Birthday(
      Date date
  ) {
    record Date(
        Integer year,
        Integer month,
        Integer day) {
    }
  }

  record Name(
      String displayName
  ) {
  }

  record EmailAddress(
      String value
  ) {
  }

  record Photo(
      String url
  ) {
  }

  public GoogleAccountDto of() {
    return new GoogleAccountDto(resourceName.substring(7), resourceName, etag, genders, birthdays, names, emailAddresses, photos);
  }

  public UserDto toUserDto() {
    String name = null, gender = null, email = null, photo=null;
    LocalDate birth = null;
    try {
      name = names.get(0).displayName;
    } catch (Exception ignored) {
    }
    try {
      birth = LocalDate.parse(String.format("%d-%02d-%02d", birthdays.get(0).date.year, birthdays.get(0).date.month, birthdays.get(0).date.day));
      DateTimeFormat.forPattern("yyyy-MM-dd");
    } catch (Exception ignored) {
    }
    try {
      gender = genders.get(0).value;
    } catch (Exception ignored) {
    }
    try {
      email = emailAddresses.get(0).value;
    } catch (Exception ignored) {
    }

    try {
      photo = photos.get(0).url;
    } catch (Exception ignored) {
    }

    return new UserDto(
        null,
        name,
        null,
        birth,
        "male".equals(gender) ? kr.finpo.api.constant.Gender.MALE : "female".equals(gender) ? kr.finpo.api.constant.Gender.FEMALE : null,
        email,
        null,
        photo,
        OAuthType.GOOGLE,
        null,
        null,
        null
    );
  }
}
