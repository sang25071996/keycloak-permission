package user.com.vn.exception;

import lombok.Builder;
import user.com.vn.common.dto.SysError;

/**
 * 
 * <p>NotFoundException</p>
 * Nov 13, 2020
 *-------------------
 * @author macbook
 *
 */
@Builder
public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private SysError sysError;

	public NotFoundException(SysError sysError) {
		this.sysError = sysError;
	}

	public NotFoundException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public NotFoundException(Throwable throwable) {
		super(throwable);
	}

	public NotFoundException(String message) {
		super(message);
	}

	public SysError getSysError() {
		return sysError;
	}

	public void setSysError(SysError sysError) {
		this.sysError = sysError;
	}
}
