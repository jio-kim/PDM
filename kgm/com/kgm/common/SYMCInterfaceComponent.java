package com.kgm.common;

public interface SYMCInterfaceComponent {

	/** 모든 대상 Component 들은 SpalmInterfaceComponent의 getValue() 메소드를 재정의 하여
	 * 선택, 입력 된 값을 반환 될 수 있도록 처리 할것. */
	public Object getValue();
}
