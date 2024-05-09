package com.kgm.common;

import java.util.EventListener;

public interface OperationAbortedListener extends EventListener {
	public abstract void operationAborted();
}
