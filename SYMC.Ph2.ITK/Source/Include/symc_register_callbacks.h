#ifndef SYMC_REGISTER_CALLBACKS_H
#define SYMC_REGISTER_CALLBACKS_H

/**
 * ���� Pre Action �� Post Action ���� ����ϴ� �Լ�
 */
extern DLLAPI int SYMC_user_init_module
(
    int *decision, ///< (I) �Լ� ����
    va_list args   ///< (I) �Լ� �Ķ����
);

#endif
