#ifndef SYMC_REGISTER_CALLBACKS_H
#define SYMC_REGISTER_CALLBACKS_H

/**
 * 각종 Pre Action 및 Post Action 등을 등록하는 함수
 */
extern DLLAPI int SYMC_user_init_module
(
    int *decision, ///< (I) 함수 결정
    va_list args   ///< (I) 함수 파라메터
);

#endif
