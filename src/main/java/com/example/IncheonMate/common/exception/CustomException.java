package com.example.IncheonMate.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
  private final ErrorCode errorCode;
  // message 필드는 굳이 따로 안 만들어도 됩니다. (부모 클래스인 RuntimeException이 이미 가지고 있음)

  public CustomException(ErrorCode errorCode){
    super(errorCode.getMessage()); // 부모에게 메시지 전달 (중요!)
    this.errorCode = errorCode;
  }

  public CustomException(ErrorCode errorCode,String message){
    super(message); // 부모에게 내가 만든 메시지 전달 (중요!)
    this.errorCode = errorCode;
  }
}
