package org.crawlify.platform.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SystemException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;

    private String msg;

    public SystemException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public SystemException(String msg) {
        super(msg);
        this.code = 500;
        this.msg = msg;
    }
}
