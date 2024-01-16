package com.ssangyong.common;

import java.util.EventListener;

public interface OperationAbortedListener extends EventListener {
	public abstract void operationAborted();
}
