#include <symc_common.h>

#ifdef __cplusplus
extern "C"
{
#endif

/**
 * 팀센타에 등록된 Activity 및 BOPOperation의 object_name을 수정할 때 영문명을 Clear 하는 함수
 */
extern DLLAPI int SYMC_save_post_method(METHOD_message_t *method_msg_tag, va_list args)
{
    int ifail = ITK_ok;
    char *prop_eng_name = NULL;
    char object_type[200] = "";
    tag_t target_tag  = method_msg_tag->object_tag;
//    tag_t item_tag = NULLTAG;

//    va_list largs;
//    va_copy(largs, args);
//    tag_t prop_tag = va_arg(largs, tag_t);
//    char *value = va_arg(largs, char *);
//    va_end( largs );

    ITK(WSOM_ask_object_type(target_tag, object_type), false);
//printf("object type is [%s]\n", object_type);

	if (strcmp(object_type, M7_BOPActivity_c) == 0)
	{
		// target에서 m7_ENG_NAME property가 존재하는지 체크해서 존재하면 Clear 한다.
		ITK(AOM_ask_value_string(target_tag, M7_Attr_Eng_Name_c, &prop_eng_name), false);
//printf("eng value => [%s]11\n", prop_eng_name);

		if (prop_eng_name != NULL && strlen(prop_eng_name) > 0)
			ITK(AOM_set_value_string(target_tag, M7_Attr_Eng_Name_c, ""), false);
	}
	else if (strcmp(object_type, M7_BOPBodyOpRevision_c) == 0 ||
			  strcmp(object_type, M7_BOPAssyOpRevision_c) == 0 ||
			  strcmp(object_type, M7_BOPPaintOpRevision_c) == 0)
	{
//		ITK(ITEM_ask_item_of_rev(target_tag, &item_tag), false);

		ITK(AOM_ask_value_string(target_tag, M7_Attr_Eng_Name_c, &prop_eng_name), false);
//printf("eng value => [%s]\n", prop_eng_name);

		if (prop_eng_name != NULL && strlen(prop_eng_name) > 0)
		{
//			ITK(AOM_lock(target_tag), false);
			ITK(AOM_set_value_string(target_tag, M7_Attr_Eng_Name_c, ""), false);
//			ITK(AOM_save(target_tag), false);
//			ITK(AOM_unlock(target_tag), false);
		}
	}

	//ITK(AOM_refresh(activity_tag, FALSE), false);

	if (prop_eng_name)
        MEM_free(prop_eng_name);

	return ifail;
}

//extern DLLAPI int SYMC_BOMLine_add_method(METHOD_message_t *method_msg_tag, va_list args)
//{
//	int ifail = ITK_ok;
//    // 파라메터 얻어오기
//    tag_t parent_tag = va_arg(args, tag_t);
//    tag_t item_tag = va_arg(args, tag_t);
//    tag_t itemRev_tag = va_arg(args, tag_t);
//    tag_t bv_tag = va_arg(args, tag_t);
//    char *occType = va_arg(args, char *);
//    tag_t *newBomline_tag = va_arg(args, tag_t *);
//    tag_t gde_tag = va_arg(args, tag_t);
//
//    tag_t parent_item_tag = NULLTAG;
//    tag_t parent_rev_tag = NULLTAG;
//    tag_t occ_tag = NULLTAG;
//    tag_t node_tag = NULLTAG;
//    char parent_item_type[ITEM_type_size_c + 1] = "";
//    char child_item_type[ITEM_type_size_c + 1] = "";
//    char *bl_supplymode = NULL;
//    char *str_occ_puid = NULL;
//    int action_type = -1;
//    int attr_parent_item = 0;
//    int attr_parent_rev = 0;
//    int attr_supplymode = 0;
//    int attr_occ_puid = 0;
//    logical is_altbop = false;
//printf("BOM occ_type => [%s][%d][%d]\n", occType, *newBomline_tag, gde_tag);
//
//    // 사용자 입력 파라메터 읽어오기 - 봄라인 생성 이전/후 값들
//    TC_init_argument_list(method_msg_tag->user_args);
//    switch (TC_next_int_argument(method_msg_tag->user_args))
//    {
//        case METHOD_pre_action_type:
//            action_type = METHOD_pre_action_type;
////            printf("\nGTAC_BOMLine_method running as pre-action:\n");
//            break;
//        case METHOD_post_action_type:
//            action_type = METHOD_post_action_type;
////            printf("\nGTAC_BOMLine_method running as post-action:\n");
//            break;
//        default:
//        	;
////            printf("\nGTAC_BOMLine_method didn't get user_args\n");
//    }
//
//    // 봄라인이 생성전이면서 할당이 MEConsumed 일때
//    if (action_type == METHOD_post_action_type && occType != NULL && strcmp(occType, "MEConsumed") == 0)
//    {
//        // 봄라인 아이템을 읽을 수 있는 변수 테그 읽기
//        ITK(BOM_line_look_up_attribute(bomAttr_lineItemTag, &attr_parent_item), false);
//        ITK(BOM_line_look_up_attribute(bomAttr_lineItemRevTag, &attr_parent_rev), false);
//        // 아이템 읽기
//        ITK(BOM_line_ask_attribute_tag(parent_tag, attr_parent_item, &parent_item_tag), false);
//        ITK(BOM_line_ask_attribute_tag(parent_tag, attr_parent_rev, &parent_rev_tag), false);
//        // 상위 아이템의 타입이 어떠한가?
//        ITK(ITEM_ask_type(parent_item_tag, parent_item_type), false);
//        ITK(AOM_ask_value_logical(parent_rev_tag, "m7_IS_ALTBOP", &is_altbop), false);
//
//        ITK(ITEM_ask_type(item_tag, child_item_type), false);
//printf("parent item type => [%s] child type [%s] is bop[%b]\n", parent_item_type, child_item_type, is_altbop);
//
//        if (! is_altbop)
//        {
//            if (strcmp(parent_item_type, "M7_BOPBodyOp") == 0 || strcmp(parent_item_type, "M7_BOPAssyOp") == 0 || strcmp(parent_item_type, "M7_BOPPaintOp") == 0)
//            {
//                // 봄라인 상위 아이템을 읽을 수 있는 변수 테그 읽기
//                ITK(BOM_line_look_up_attribute("S7_SUPPLY_MODE", &attr_supplymode), false);//S7_SUPPLY_MODE
//                ITK(BOM_line_look_up_attribute(bomAttr_lineOccTag, &attr_occ_puid), false);
//                // 아이템 읽기
//                ITK(BOM_line_ask_attribute_string(*newBomline_tag, attr_supplymode, &bl_supplymode), false);
//                ITK(BOM_line_ask_attribute_tag(*newBomline_tag, attr_occ_puid, &occ_tag), false);
//
//printf("occurrence supply mode => [%s]\n", bl_supplymode);
//ITK(SYMC_check_me_consumed(occ_tag, &node_tag), false);
//printf("pathnode tag => [%d]\n", node_tag);
//                ITK(SYMC_get_SModeValue_FromEBOM(occ_tag, &bl_supplymode), false);
//AOM_tag_to_string(occ_tag, &str_occ_puid);
//printf("supply mode => [%s][%d] occtag[%d][%s]\n", bl_supplymode, attr_supplymode, occ_tag, str_occ_puid);
//if (str_occ_puid)
//	MEM_free(str_occ_puid);
//
//                if (bl_supplymode == NULL || (strlen(bl_supplymode) >= 0 &&
//                		 strcmp(bl_supplymode, "C0 P7") != 0 &&
//                		 strcmp(bl_supplymode, "C1 P7") != 0 &&
//                		 strcmp(bl_supplymode, "C7 P7") != 0 &&
//                		 strcmp(bl_supplymode, "G0 P7") != 0 &&
//                		 strcmp(bl_supplymode, "P1") != 0 &&
//                		 strcmp(bl_supplymode, "P7") != 0 &&
//                		 strcmp(bl_supplymode, "PD") != 0))
//                {
//                	ifail = BOM_line_cut(*newBomline_tag);
//printf("bom line cut.[%d]\n", ifail);
//
//        			EMH_store_error_s1(EMH_severity_error, EMH_USER_error_base + 300, "selected EBOM Item is not SupplyMode assign to BOPOperation.");
//        			TC_write_syslog("[%s]\t[%d]\n\tBOP attach error.\n", __FILE__, __LINE__);
//        			return EMH_USER_error_base + 300;
//                }
//
//                if (bl_supplymode)
//                	MEM_free(bl_supplymode);
//printf("is it?\n");
//            }
//            else
//            {
//            	ifail = BOM_line_cut(*newBomline_tag);
//printf("bom line cut.[%d]\n", ifail);
//
//    			EMH_store_error_s1(EMH_severity_error, EMH_USER_error_base + 300, "EBOM Item can only assign to BOPOperation.");
//    			TC_write_syslog("[%s]\t[%d]\n\tBOP attach error.\n", __FILE__, __LINE__);
//    			return EMH_USER_error_base + 300;
//            }
//        }
//    }
//printf("return value ifail => [%d]\n", ifail);
//
//    return ifail;
//}

extern DLLAPI int SYMC_check_me_consumed(tag_t bopocc_puid_tag, tag_t *node_tag)
{
	int ifail = ITK_ok;
    int enq_rows = 0;
    int enq_cols = 0;
    void ***enq_objs = NULL;
    char *select_attr_list[] = {"puid"};
    char *querySelectSMODEEnqId = "SYMC_get_smode_query1" ;
    tag_t meapp_type_tag = NULLTAG;

    ITK(GRM_find_relation_type("IMAN_MEAppearance", &meapp_type_tag), false);

    ITK(POM_enquiry_create(querySelectSMODEEnqId), false);
    ITK(POM_enquiry_create_class_alias(querySelectSMODEEnqId, "IMANRELATION", true, "rel_alias" ), false);
    ITK(POM_enquiry_create_class_alias(querySelectSMODEEnqId, "MEAPPEARANCEPATHNODE", true, "meapp_alias" ), false);

    ITK(POM_enquiry_add_select_attrs(querySelectSMODEEnqId, "meapp_alias", 1, select_attr_list), false);

    // 1. relation primary object 조회 조건
    ITK(POM_enquiry_set_tag_expr(querySelectSMODEEnqId, "wherePrimaryOccExpr", "rel_alias", "PRIMARY_OBJECT", POM_enquiry_equal, bopocc_puid_tag), false);
    // 2. relation type 조회 조건
    ITK(POM_enquiry_set_tag_expr(querySelectSMODEEnqId, "whereRelationTypeExpr", "rel_alias", "RELATION_TYPE", POM_enquiry_equal, meapp_type_tag), false);
    // 3. relation secondary object와 PathNode Join
    ITK(POM_enquiry_set_join_expr(querySelectSMODEEnqId, "whereJoinRelNMEApp", "rel_alias", "SECONDARY_OBJECT", POM_enquiry_equal, "meapp_alias", "PUID"), false);

    // 1번부터 10번까지 where 조건 Join
    ITK(POM_enquiry_set_expr(querySelectSMODEEnqId, "subWhereExprId_01", "wherePrimaryOccExpr", POM_enquiry_and, "whereRelationTypeExpr"), false);
    ITK(POM_enquiry_set_expr(querySelectSMODEEnqId, "subWhereExprId_02", "subWhereExprId_01", POM_enquiry_and, "whereJoinRelNMEApp"), false);
    printf("is here?[%d].\n", __LINE__);

	// Where 조건 지정
	ITK(POM_enquiry_set_where_expr(querySelectSMODEEnqId, "subWhereExprId_02"), false);
printf("is here?[%d].\n", __LINE__);

	// 조회 실행
	ITK(POM_enquiry_execute(querySelectSMODEEnqId, &enq_rows, &enq_cols, &enq_objs), false);
printf("is here?[%d].\n", __LINE__);

	// 조회했으니까 조회는 삭제
	ITK(POM_enquiry_delete(querySelectSMODEEnqId), false);
printf("[%d] quired\n", enq_rows);
	if (enq_rows > 0)
	{
printf("is here?.\n");
	node_tag = (tag_t *) enq_objs[0][0];
printf("is here?.\n");

		MEM_free(enq_objs);
	}

	return ifail;
}

//extern DLLAPI int SYMC_get_SModeValue_FromEBOM(tag_t bopocc_puid_tag, char **str_smode)
//{
//    int ifail = ITK_ok;
//    int enq_rows = 0;
//    int enq_cols = 0;
//    void ***enq_objs = NULL;
//    char *select_attr_list[] = {"puid", "parent_bvr"};
//    char *select_attr_max_list[] = {"SYMC_sub_max_revision"};
//    char *querySelectSMODEEnqId = "SYMC_get_smode_query" ;
//    char *querySelectSMODESubEnqId = "SYMC_get_smode_sub_maxrev_query";
//    tag_t meapp_type_tag = NULLTAG;
//    tag_t smode_note_tag = NULLTAG;
//    tag_t s7_released_tag = NULLTAG;
//    tag_t parent_bvr_tag = NULLTAG;
//    tag_t occ_tag = NULLTAG;
//printf("is here?[%d].\n", __LINE__);
//
//    ITK(GRM_find_relation_type("IMAN_MEAppearance", &meapp_type_tag), false);
//    ITK(PS_find_note_type("S7_SUPPLY_MODE", &smode_note_tag), false);
//    ITK(CR_find_status_type("S7_Released", &s7_released_tag), false);
//printf("is here?[%d].\n", __LINE__);
//
//    ITK(POM_enquiry_create(querySelectSMODEEnqId), false);
//    ITK(POM_enquiry_create_class_alias(querySelectSMODEEnqId, "IMANRELATION", true, "rel_alias" ), false);
//    ITK(POM_enquiry_create_class_alias(querySelectSMODEEnqId, "MEAPPEARANCEPATHNODE", true, "meapp_alias" ), false);
//    ITK(POM_enquiry_create_class_alias(querySelectSMODEEnqId, "PSOCCURRENCE", true, "occ_alias" ), false);
//    ITK(POM_enquiry_create_class_alias(querySelectSMODEEnqId, "ITEMREVISION", true, "rev_alias" ), false);
//printf("is here?[%d].\n", __LINE__);
//
//    ITK(POM_enquiry_add_select_attrs(querySelectSMODEEnqId, "occ_alias", 2, select_attr_list), false);
//printf("is here?[%d].\n", __LINE__);
//
//    // 1. relation primary object 조회 조건
//    ITK(POM_enquiry_set_tag_expr(querySelectSMODEEnqId, "wherePrimaryOccExpr", "rel_alias", "PRIMARY_OBJECT", POM_enquiry_equal, bopocc_puid_tag), false);
//    // 2. relation type 조회 조건
//    ITK(POM_enquiry_set_tag_expr(querySelectSMODEEnqId, "whereRelationTypeExpr", "rel_alias", "RELATION_TYPE", POM_enquiry_equal, meapp_type_tag), false);
//    // 3. relation secondary object와 PathNode Join
//    ITK(POM_enquiry_set_join_expr(querySelectSMODEEnqId, "whereJoinRelNMEApp", "rel_alias", "SECONDARY_OBJECT", POM_enquiry_equal, "meapp_alias", "PUID"), false);
//    // 4. PathNode 와 Occurrence의 occ_thread Join
//    ITK(POM_enquiry_set_join_expr(querySelectSMODEEnqId, "whereJoinMEAppNOcc", "meapp_alias", "OCC_THREAD", POM_enquiry_equal, "occ_alias", "OCC_THREAD"), false);
//    // 5. Occurrence의 parent_bvr과 Revision Join
//    ITK(POM_enquiry_set_join_expr(querySelectSMODEEnqId, "whereJoinOccNStr", "occ_alias", "PARENT_BVR", POM_enquiry_equal, "rev_alias", "structure_revisions"), false);
//    // 6. Release 된 마지막 Revision 찾는 Sub Query
//    ITK(POM_enquiry_set_sub_enquiry(querySelectSMODEEnqId, querySelectSMODESubEnqId), false);
//    ITK(POM_enquiry_create_class_alias(querySelectSMODESubEnqId, "ITEMREVISION", true, "sub_rev_alias"), false);
//printf("is here?[%d].\n", __LINE__);
//    ITK(POM_enquiry_set_attr_expr(querySelectSMODESubEnqId, select_attr_max_list[0], "sub_rev_alias", "ITEM_REVISION_ID", POM_enquiry_max, ""), false);
//printf("is here?[%d].\n", __LINE__);
//    ITK(POM_enquiry_add_select_exprs(querySelectSMODESubEnqId, 1, select_attr_max_list), false);
//printf("is here?[%d].\n", __LINE__);
//    ITK(POM_enquiry_set_tag_expr(querySelectSMODESubEnqId, "subWhereJoinReleased", "sub_rev_alias", "release_status_list", POM_enquiry_in, s7_released_tag), false);
//printf("is here?[%d].\n", __LINE__);
//    ITK(POM_enquiry_set_join_expr(querySelectSMODESubEnqId, "subWhereJoinRevExpr_1", "sub_rev_alias", "ITEMS_TAG", POM_enquiry_equal, "rev_alias", "ITEMS_TAG"), false);
//printf("is here?[%d].\n", __LINE__);
//    ITK(POM_enquiry_set_expr(querySelectSMODESubEnqId, "subWhereMaxExpr", "subWhereJoinReleased", POM_enquiry_and, "subWhereJoinRevExpr_1"), false);
//printf("is here?[%d].\n", __LINE__);
//    ITK(POM_enquiry_set_where_expr(querySelectSMODESubEnqId, "subWhereMaxExpr"), false);
//    ITK(POM_enquiry_set_attr_expr(querySelectSMODEEnqId, "whereMaxRevEqualExprId", "rev_alias", "ITEM_REVISION_ID", POM_enquiry_equal, querySelectSMODESubEnqId), false);
//printf("is here?[%d].\n", __LINE__);
//
////ITK( POM_enquiry_set_sub_enquiry( querySelectSMODEEnqId, querySelectSMODESubEnqId ), false );
////ITK( POM_enquiry_create_class_alias( querySelectSMODESubEnqId, "ITEM", true, "sub_item_alilas" ), false);
////ITK( POM_enquiry_create_class_alias( querySelectSMODESubEnqId, "ITEMREVISION", true, "sub_rev_alilas" ), false);
////printf("is here?[%d].\n", __LINE__);
////
////ITK( POM_enquiry_set_attr_expr( querySelectSMODESubEnqId, select_attr_max_list[0], "sub_rev_alilas", "ITEM_REVISION_ID", POM_enquiry_max, "" ), false );
////printf("is here?[%d].\n", __LINE__);
////ITK( POM_enquiry_add_select_exprs( querySelectSMODESubEnqId, 1, select_attr_max_list ), false );
////printf("is here?[%d].\n", __LINE__);
////
////ITK( POM_enquiry_set_join_expr( querySelectSMODESubEnqId, "subWhereJoinRevExpr_1", "sub_item_alilas", "PUID", POM_enquiry_equal, "sub_rev_alilas", "ITEMS_TAG"), false);
////ITK( POM_enquiry_set_join_expr( querySelectSMODESubEnqId, "subWhereJoinRevExpr_2", "sub_item_alilas", "PUID", POM_enquiry_equal, "rev_alias", "ITEMS_TAG"), false);
////printf("is here?[%d].\n", __LINE__);
////
////ITK( POM_enquiry_set_expr( querySelectSMODESubEnqId, "subWhereExprId_02", "subWhereJoinRevExpr_1", POM_enquiry_and, "subWhereJoinRevExpr_2" ), false );
////printf("is here?[%d].\n", __LINE__);
////
////ITK( POM_enquiry_set_where_expr( querySelectSMODESubEnqId, "subWhereExprId_02" ), false );
////printf("is here?[%d].\n", __LINE__);
////
////ITK( POM_enquiry_set_attr_expr( querySelectSMODEEnqId, "whereMaxRevEqualExprId", "rev_alias", "ITEM_REVISION_ID", POM_enquiry_equal, querySelectSMODESubEnqId ), false );
////printf("is here?[%d].\n", __LINE__);
//
//
//    // 1번부터 10번까지 where 조건 Join
//    ITK(POM_enquiry_set_expr(querySelectSMODEEnqId, "subWhereExprId_01", "wherePrimaryOccExpr", POM_enquiry_and, "whereRelationTypeExpr"), false);
//    ITK(POM_enquiry_set_expr(querySelectSMODEEnqId, "subWhereExprId_02", "subWhereExprId_01", POM_enquiry_and, "whereJoinRelNMEApp"), false);
//    ITK(POM_enquiry_set_expr(querySelectSMODEEnqId, "subWhereExprId_03", "subWhereExprId_02", POM_enquiry_and, "whereJoinMEAppNOcc"), false);
//    ITK(POM_enquiry_set_expr(querySelectSMODEEnqId, "subWhereExprId_04", "subWhereExprId_03", POM_enquiry_and, "whereJoinOccNStr"), false);
//    ITK(POM_enquiry_set_expr(querySelectSMODEEnqId, "subWhereExprId_05", "subWhereExprId_04", POM_enquiry_and, "whereMaxRevEqualExprId"), false);
//printf("is here?[%d].\n", __LINE__);
//
//    // Where 조건 지정
//    ITK(POM_enquiry_set_where_expr(querySelectSMODEEnqId, "subWhereExprId_05"), false);
//printf("is here?[%d].\n", __LINE__);
//
//    // 조회 실행
//    ITK(POM_enquiry_execute(querySelectSMODEEnqId, &enq_rows, &enq_cols, &enq_objs), false);
//printf("is here?[%d].\n", __LINE__);
//
//    // 조회했으니까 조회는 삭제
//    ITK(POM_enquiry_delete(querySelectSMODEEnqId), false);
//printf("[%d] quired\n", enq_rows);
//    if (enq_rows > 0)
//    {
//printf("is here?.\n");
//        parent_bvr_tag = (tag_t) enq_objs[0][0];
//printf("is here?.\n");
//        occ_tag = (tag_t) enq_objs[0][1];
//printf("is here?.\n");
//
//        ITK(PS_ask_occurrence_note_text(parent_bvr_tag, occ_tag, smode_note_tag, str_smode), false);
//printf("is here?.\n");
//
//        MEM_free(enq_objs);
//    }
//
//    return ifail;
//}


/**
 * Activity를 생성할 때, 공법의 MECO를 가져와서 설정하는 함수
 */
//extern DLLAPI int SYMC_activity_crete_post_method(METHOD_message_t *method_msg_tag, va_list args)
//{
//	return SYMC_set_activity_meco_no(method_msg_tag->object_tag);

/*    int ifail = ITK_ok;
    tag_t activity_tag  = method_msg_tag->object_tag;
//    char *qry_act_root = "query_activity_root";
//    char *qry_act_op = "query_activity_operation";
//    char *select_attr_list[] = {"puid"};
//    char *select_meco_list[] = {"m7_meco_no"};
//    int enq_rows = 0;
//    int enq_cols = 0;
//    void ***enq_objs = NULL;
//    tag_t activity_root_tag = NULLTAG;
    char *bopop_meco_no = NULL;
    char *act_meco_no = NULL;
//    tag_t prop_meco_tag = NULLTAG;
//    tag_t prop_meco_desc_tag = NULLTAG;

    int n_actroot_refs = 0;
    int *n_actroot_levels = 0;
    tag_t *actroot_tags = NULLTAG;
    char **actroot_rel_names = NULL;
    int n_bopop_refs = 0;
    int *n_bopop_levels = 0;
    tag_t *bopop_tags = NULLTAG;
    char **bopop_rel_names = NULL;
    char object_type[WSO_name_size_c + 1] = "";
//    char object_name[WSO_name_size_c + 1] = "";
    tag_t parent_object_tag = NULLTAG;
    tag_t parent_type_tag = NULLTAG;
    tag_t meactivity_type_tag = NULLTAG;
    int i = 0, j = 0;

printf("this line add activity line print..parameter count\n");
	ITK(WSOM_ask_object_type(activity_tag, object_type), false);
printf("object type is [%s]\n", object_type);

	ITK(AOM_get_value_string(activity_tag, M7_Attr_MECO_NO_c, &act_meco_no), false);

	ITK(TCTYPE_find_type("M7_BOPOperationRevision", NULL, &meactivity_type_tag), false);

	ITK(WSOM_where_referenced(activity_tag, 1, &n_actroot_refs, &n_actroot_levels, &actroot_tags, &actroot_rel_names), false);
printf("[%d] line returns.\n", n_actroot_refs);
	for (i = 0; i < n_actroot_refs; i++)
	{
		ITK(WSOM_where_referenced(actroot_tags[i], 1, &n_bopop_refs, &n_bopop_levels, &bopop_tags, &bopop_rel_names), false);

		for (j = 0; j < n_bopop_refs; j++)
		{
			// Reference 상위의 Object Type
			ITK(WSOM_ask_object_type(bopop_tags[i], object_type), false);
printf("parent object type[%s]\n", object_type);
			// Reference 상위의 Class Object
			ITK(TCTYPE_ask_object_type(actroot_tags[i], &parent_object_tag), false);
			ITK(TCTYPE_ask_parent_type(parent_object_tag, &parent_type_tag), false);
			// Reference 상위의 Class Type
			ITK(TCTYPE_ask_name(parent_type_tag, object_type), false);
printf("parent class type[%s]\n", object_type);

			if (parent_type_tag == meactivity_type_tag)
			{
				ITK(AOM_get_value_string(bopop_tags[j], M7_Attr_MECO_NO_c, &bopop_meco_no), false);

				if (bopop_meco_no != act_meco_no)
				{
printf("set meco change value[%s] to [%s]\n", act_meco_no, bopop_meco_no);
					ITK(AOM_set_value_string(activity_tag, M7_Attr_MECO_NO_c, bopop_meco_no), false);
				}
			}
		}
	}

	// 현재 등록되는 Activity의 Root Activity를 찾는다.
//	ITK(POM_enquiry_create(qry_act_root), false);
//	ITK(POM_enquiry_create_class_alias(qry_act_root, "CONTENTS", true, "contents_alias"), false);
//	ITK(POM_enquiry_add_select_attrs(qry_act_root, "contents_alias", 1, select_attr_list), false);
//	ITK(POM_enquiry_set_tag_expr(qry_act_root, "contents_list_expr", "CONTENTS", "valu_0", POM_enquiry_equal, activity_tag), false);
//	ITK(POM_enquiry_set_where_expr(qry_act_root, "contents_list_expr"), false);
//	ITK(POM_enquiry_execute(qry_act_root, &enq_rows, &enq_cols, &enq_objs), false);
//	ITK(POM_enquiry_delete(qry_act_root), false);
//
////	activity_root_tag = (tag_t *) MEM_alloc(sizeof(tag_t) * (enq_rows + 1));
//	if (enq_rows == 0)
//	{
//		EMH_store_error_s1(EMH_severity_error, EMH_USER_error_base + 100, "Activity root not found. contact to BOPAdmin.");
//		TC_write_syslog("[%s]\t[%d]\n\tActivity Root not found.\n", __FILE__, __LINE__);
//		return EMH_USER_error_base + 100;
//	}
//	if (enq_rows != 1)
//	{
//		EMH_store_error_s1(EMH_severity_error, EMH_USER_error_base + 101, "Activity root found many. contact to BOPAdmin.");
//		TC_write_syslog("[%s]\t[%d]\n\tActivity Root found many[%d].\n", __FILE__, __LINE__, enq_rows);
//		return EMH_USER_error_base + 101;
//	}
//	activity_root_tag = *(tag_t *)(enq_objs[0][0]);
//	if (enq_objs)
//		MEM_free(enq_objs);
//	enq_rows = 0;
//	enq_cols = 0;

	// Root Activity가 연결된 BOPOperation을 찾아 그 속성 값을 읽는다.
//	ITK(POM_enquiry_create(qry_act_op), false);
//	ITK(POM_enquiry_create_class_alias(qry_act_op, ME_activity_class_name_c, true, "activity_alias"), false);
//	ITK(POM_enquiry_create_class_alias(qry_act_op, ME_meop_rev_class_name_c, true, "op_alias"), false);
//	ITK(POM_enquiry_create_class_alias(qry_act_op, M7_BOPOperationRevision_c, true, "bopop_alias"), false);
//
//	ITK(POM_enquiry_add_select_attrs(qry_act_op, "bopop_alias", 1, select_meco_list), false);
//
//	ITK(POM_enquiry_set_join_expr(qry_act_op, "whereJoinActivity", "activity_alias", "puid", POM_enquiry_equal, "op_alias", "root_activity"), false);
//	ITK(POM_enquiry_set_join_expr(qry_act_op, "whereJoinOp", "op_alias", "puid", POM_enquiry_equal, "bopop_alias", "puid"), false);
//	ITK(POM_enquiry_set_tag_expr(qry_act_op, "act_root_expr", "activity_alias", "puid", POM_enquiry_equal, activity_root_tag), false);
//	ITK(POM_enquiry_set_expr(qry_act_op, "whereAnd1", "whereJoinActivity", POM_enquiry_and, "whereJoinOp"), false);
//	ITK(POM_enquiry_set_expr(qry_act_op, "whereAnd2", "whereAnd1", POM_enquiry_and, "act_root_expr"), false);
//	ITK(POM_enquiry_set_where_expr(qry_act_op, "whereAnd2"), false);
//
//	ITK(POM_enquiry_execute(qry_act_op, &enq_rows, &enq_cols, &enq_objs), false);
//	ITK(POM_enquiry_delete(qry_act_op), false);
//	if (enq_rows == 0)
//	{
//		EMH_store_error_s1(EMH_severity_error, EMH_USER_error_base + 102, "BOPOperation not found. contact to BOPAdmin.");
//		TC_write_syslog("[%s]\t[%d]\n\tBOPOperation not found.\n", __FILE__, __LINE__);
//		return EMH_USER_error_base + 102;
//	}
//	if (enq_rows != 1)
//	{
//		EMH_store_error_s1(EMH_severity_error, EMH_USER_error_base + 103, "BOPOperation found many. contact to BOPAdmin.");
//		TC_write_syslog("[%s]\t[%d]\n\tBOPOperation found many[%d].\n", __FILE__, __LINE__, enq_rows);
//		return EMH_USER_error_base + 103;
//	}
//	meco_no = (char *)(enq_objs[0][0]);
//
//	ITK(AOM_refresh(activity_tag, true), false);
//	ITK(PROP_ask_property_by_name(activity_tag, M7_Attr_MECO_NO_c, &prop_meco_tag), false);
//	ITK(PROP_ask_descriptor(prop_meco_tag, &prop_meco_desc_tag), false);
//	ITK(PROPDESC_set_protection(prop_meco_desc_tag, PROP_write), false);
//	ITK(PROP_set_value_string(prop_meco_tag, meco_no), false);
//	ITK(AOM_save(activity_tag), false);
//	ITK(AOM_unlock(activity_tag), false);
//
//	if (enq_objs)
//		MEM_free(enq_objs);
//	if (meco_no)
//		MEM_free(meco_no);

    return ifail;*/
//}

//extern DLLAPI int SYMC_set_activity_meco_no(tag_t activity_tag)
//{
//	int ifail = ITK_ok;
//    char *bopop_meco_no = NULL;
//    char *act_meco_no = NULL;
//    int n_actroot_refs = 0;
//    int *n_actroot_levels = 0;
//    tag_t *actroot_tags = NULLTAG;
//    char **actroot_rel_names = NULL;
//    int n_bopop_refs = 0;
//    int *n_bopop_levels = 0;
//    tag_t *bopop_tags = NULLTAG;
//    char **bopop_rel_names = NULL;
//    char object_type[WSO_name_size_c + 1] = "";
//    tag_t parent_object_tag = NULLTAG;
//    tag_t parent_type_tag = NULLTAG;
//    tag_t mebopop_type_tag = NULLTAG;
//    int i = 0, j = 0;
//
//	ITK(WSOM_ask_object_type(activity_tag, object_type), false);
////printf("object type is [%s]\n", object_type);
//
//	ITK(AOM_get_value_string(activity_tag, M7_Attr_MECO_NO_c, &act_meco_no), false);
//
//	ITK(TCTYPE_find_type(M7_BOPOperationRevision_c, NULL, &mebopop_type_tag), false);
//
//	ITK(WSOM_where_referenced2(activity_tag, 1, &n_actroot_refs, &n_actroot_levels, &actroot_tags, &actroot_rel_names), false);
////printf("[%d] line returns.\n", n_actroot_refs);
//	for (i = 0; i < n_actroot_refs; i++)
//	{
//		ITK(WSOM_where_referenced2(actroot_tags[i], 1, &n_bopop_refs, &n_bopop_levels, &bopop_tags, &bopop_rel_names), false);
//
//		for (j = 0; j < n_bopop_refs; j++)
//		{
//			// Reference 상위의 Object Type
//			ITK(WSOM_ask_object_type(bopop_tags[j], object_type), false);
////printf("parent object type[%s]\n", object_type);
//			// Reference 상위의 Class Object
//			ITK(TCTYPE_ask_object_type(bopop_tags[j], &parent_object_tag), false);
//			ITK(TCTYPE_ask_parent_type(parent_object_tag, &parent_type_tag), false);
//			// Reference 상위의 Class Type
//			ITK(TCTYPE_ask_name(parent_type_tag, object_type), false);
////printf("parent class type[%s]\n", object_type);
//
//			if (parent_type_tag == mebopop_type_tag)
//			{
//				ITK(AOM_get_value_string(bopop_tags[j], M7_Attr_MECO_NO_c, &bopop_meco_no), false);
//
//				if (strcmp(bopop_meco_no, act_meco_no) != 0)
//				{
////printf("set meco change value[%s] to [%s]\n", act_meco_no, bopop_meco_no);
//					ITK(AOM_set_value_string(activity_tag, M7_Attr_MECO_NO_c, bopop_meco_no), false);
//				}
//			}
//		}
//	}
//
//    return ifail;
//}

#ifdef __cplusplus
}
#endif

