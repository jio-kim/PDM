#include <symc_common.h>
#include <symc_register_callbacks.h>

//declare the function rename_item_register
#ifdef __cplusplus
extern "C"
{
#endif

/**
 * 모듈 등을 로드할 수 있도록 설정하는 함수
 */
extern DLLAPI int libsymcmbom_register_callbacks()
{
    printf("SSangyong BOP Init Module\n");
    TC_write_syslog("SSangyong BOP Init Module\n");

    // BOMLine의 PostAction 및 Revision의 삭제 PreAction 기능을 설정하는 함수 호출
    CUSTOM_register_exit( "libsymcmbom",
                          "USER_init_module",
                          (CUSTOM_EXIT_ftn_t)SYMC_user_init_module );

    return ITK_ok;
}

/**
 * 각종 Pre Action 및 Post Action 등을 등록하는 함수
 */
extern DLLAPI int SYMC_user_init_module(int *decision, va_list args)
{
    int ifail = ITK_ok;
    char addClassName[20] = "BOMLine";
    METHOD_id_t item_method;
    METHOD_id_t inited_method;
    METHOD_id_t activity_save_post_method;
    METHOD_id_t bopop_save_post_method;
    METHOD_id_t activity_add_child_method;
    METHOD_id_t bomline_add_method;
    TC_argument_list_t *myArgs;

    *decision = ALL_CUSTOMIZATIONS;

/*
    ITK(METHOD_find_method(addClassName, BOMLine_add_msg, &bomline_add_method), false);
    if (bomline_add_method.id != NULLTAG)
    {
        myArgs = (TC_argument_list_t *) MEM_alloc( sizeof(TC_argument_list_t));
        myArgs->number_of_arguments = 1;
        myArgs->arguments = (TC_argument_t *) MEM_alloc( sizeof(TC_argument_t));
        myArgs->arguments[0].type = POM_int;
        myArgs->arguments[0].array_size = 1;
        myArgs->arguments[0].val_union.int_value = METHOD_post_action_type;

        // 봄라인의 Add 기능에 대한 Pre-Action 기능 추가
        ITK(METHOD_add_action(bomline_add_method, METHOD_post_action_type, (METHOD_function_t) SYMC_BOMLine_add_method, myArgs), false);
    }
    else
        printf("Method not found!\n");
*/


/*
    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_CATEGORY_c, PROP_set_value_string_at_msg, &activity_add_child_method), false);

    if (activity_add_child_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL), false);
        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_CATEGORY_c, PROP_set_value_string_at_msg, SYMC_activity_crete_post_method, NULL, &activity_add_child_method), false);
        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }

    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_CODE_c, PROP_UIF_set_value_msg, &activity_add_child_method), false);

    if (activity_add_child_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL), false);
        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_CODE_c, PROP_UIF_set_value_msg, SYMC_activity_crete_post_method, NULL, &activity_add_child_method), false);
        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }

    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_CONTROL_BASIS_c, PROP_set_value_string_msg, &activity_add_child_method), false);

    if (activity_add_child_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL), false);
        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_CONTROL_BASIS_c, PROP_set_value_string_msg, SYMC_activity_crete_post_method, NULL, &activity_add_child_method), false);
        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }

    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_CONTROL_POINT_c, PROP_UIF_set_value_msg, &activity_add_child_method), false);

    if (activity_add_child_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL), false);
        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_CONTROL_POINT_c, PROP_UIF_set_value_msg, SYMC_activity_crete_post_method, NULL, &activity_add_child_method), false);
        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }

    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_OBJECT_DESC_c, PROP_UIF_set_value_msg, &activity_add_child_method), false);

    if (activity_add_child_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL), false);
        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_OBJECT_DESC_c, PROP_UIF_set_value_msg, SYMC_activity_crete_post_method, NULL, &activity_add_child_method), false);
        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }

    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_FREQUENCY_c, PROP_set_value_double_msg, &activity_add_child_method), false);

    if (activity_add_child_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL), false);
        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_FREQUENCY_c, PROP_set_value_double_msg, SYMC_activity_crete_post_method, NULL, &activity_add_child_method), false);
        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }

    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_UNIT_TIME_c, PROP_UIF_set_value_msg, &activity_add_child_method), false);

    if (activity_add_child_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL), false);
        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_UNIT_TIME_c, PROP_UIF_set_value_msg, SYMC_activity_crete_post_method, NULL, &activity_add_child_method), false);
        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }

    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_OVERTYPE_c, "PROP_assign_value_string", &activity_add_child_method), false);

    if (activity_add_child_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL), false);
        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_OVERTYPE_c, "PROP_assign_value_string", SYMC_activity_crete_post_method, NULL, &activity_add_child_method), false);
        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }

    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_FEEDING_c, PROP_set_value_int_msg, &activity_add_child_method), false);

    if (activity_add_child_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL), false);
        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_FEEDING_c, PROP_set_value_int_msg, SYMC_activity_crete_post_method, NULL, &activity_add_child_method), false);
        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }

    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_WORKERS_c, PROP_set_value_string_msg, &activity_add_child_method), false);

    if (activity_add_child_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL), false);
        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_WORKERS_c, PROP_set_value_string_msg, SYMC_activity_crete_post_method, NULL, &activity_add_child_method), false);
        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
    }
*/


/*
//    if ((ifail = METHOD_find_prop_method("MEActivity", "contents", "PROP_set_value_tags", &activity_add_child_method)) != ITK_ok)
//    {
//    	return ifail;
//    }
//
//    if (activity_add_child_method.id != NULLTAG)
//    {
//        // add new save post action of M7_BOPActivity
//        if ((ifail = METHOD_add_action(activity_add_child_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL)) != ITK_ok)
//        {
//            return ifail;
//        }
//        printf("[CfgActivityLine] Added SYMC_activity_create_post_method as Post-Action for Create Action\n");
//    }
//    else
//    {
//        // register new delete method of item revision
//        if ((ifail = METHOD_register_prop_method("MEActivity", "contents", "PROP_set_value_tags", SYMC_activity_crete_post_method, NULL, &activity_add_child_method)) != ITK_ok)
//        {
//            return ifail;
//        }
//        printf("[CfgActivityLine] Registered SYMC_activity_create_post_method as Post-Action for Create Action\n");
//    }
*/

//    if ((ifail = METHOD_find_method(M7_BOPActivity_c, ITEM_create_msg, &activity_create_post_method)) != ITK_ok)
//    {
//    	return ifail;
//    }
//
//    if (activity_create_post_method.id != NULLTAG)
//    {
//        // add new save post action of M7_BOPActivity
//        if ((ifail = METHOD_add_action(activity_create_post_method, METHOD_post_action_type, (METHOD_function_t) SYMC_activity_crete_post_method, NULL)) != ITK_ok)
//        {
//            return ifail;
//        }
//        printf("[%s] Added SYMC_activity_crete_post_method as Post-Action for Create Action\n", M7_BOPActivity_c);
//    }
//    else
//    {
//        // register new delete method of item revision
//        if ((ifail = METHOD_register_method(M7_BOPActivity_c, ITEM_create_msg, SYMC_activity_crete_post_method, NULL, &activity_create_post_method)) != ITK_ok)
//        {
//            return ifail;
//        }
//        printf("[%s] Registered SYMC_activity_crete_post_method as Post-Action for Create Action\n", M7_BOPActivity_c);
//    }

    // find save method of M7_BOPActivity
    ITK(METHOD_find_prop_method(M7_BOPActivity_c, M7_Attr_OBJECT_NAME_c, PROP_set_value_string_msg, &activity_save_post_method), false);

    if (activity_save_post_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(activity_save_post_method, METHOD_post_action_type, (METHOD_function_t) SYMC_save_post_method, NULL), false);
//        printf("[M7_BOPActivity] Added SYMC_save_post_method as Post-Action for Save Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPActivity_c, M7_Attr_OBJECT_NAME_c, PROP_set_value_string_msg, SYMC_save_post_method, NULL, &activity_save_post_method), false);
//        printf("[M7_BOPActivity] Registered SYMC_save_post_method as Post-Action for Save Action\n");
    }

    // find save method of M7_BOPActivity
    ITK(METHOD_find_prop_method(M7_BOPOperationRevision_c, M7_Attr_OBJECT_NAME_c, PROP_set_value_string_msg, &bopop_save_post_method), false);

    if (bopop_save_post_method.id != NULLTAG)
    {
        // add new save post action of M7_BOPActivity
        ITK(METHOD_add_action(bopop_save_post_method, METHOD_post_action_type, (METHOD_function_t) SYMC_save_post_method, NULL), false);
//        printf("[M7_BOPOperation] Added SYMC_save_post_method as Post-Action for Save Action\n");
    }
    else
    {
        // register new delete method of item revision
        ITK(METHOD_register_prop_method(M7_BOPOperation_c, M7_Attr_OBJECT_NAME_c, PROP_set_value_string_msg, SYMC_save_post_method, NULL, &bopop_save_post_method), false);
//        printf("[M7_BOPOperation] Registered SYMC_save_post_method as Post-Action for Save Action\n");
    }

    return ITK_ok;
}


#ifdef __cplusplus
}
#endif
