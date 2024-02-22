#ifndef SYMC_COMMON_H
#define SYMC_COMMON_H

// MEActivity 타입
#define M7_BOPActivity_c "M7_BOPActivity"
#define M7_BOPOperation_c "M7_BOPOperation"
#define M7_BOPOperationRevision_c "M7_BOPOperationRevision"
#define M7_BOPBodyOpRevision_c "M7_BOPBodyOpRevision"
#define M7_BOPAssyOpRevision_c "M7_BOPAssyOpRevision"
#define M7_BOPPaintOpRevision_c "M7_BOPPaintOpRevision"

#define M7_Attr_Eng_Name_c "m7_ENG_NAME"
#define M7_Attr_MECO_NO_c "m7_MECO_NO"

#define M7_Attr_CONTROL_BASIS_c "m7_CONTROL_BASIS"
#define M7_Attr_CONTROL_POINT_c "m7_CONTROL_POINT" // lov
#define M7_Attr_FEEDING_c "m7_FEEDING" // int
#define M7_Attr_WORKERS_c "m7_WORKERS"
#define M7_Attr_OVERTYPE_c "m7_WORK_OVERLAP_TYPE" // lov
#define M7_Attr_CATEGORY_c "time_system_category" // lov
#define M7_Attr_CODE_c "time_system_code"
#define M7_Attr_FREQUENCY_c "time_system_frequency" // double
#define M7_Attr_UNIT_TIME_c "time_system_unit_time" // double
#define M7_Attr_OBJECT_DESC_c "long_description"
#define M7_Attr_OBJECT_NAME_c "object_name"

#ifndef WNT
#include <unistd.h>
#define SYSTEM_COPY_COMMAND_s "cp"
#define SYSTEM_MOVE_COMMAND_s "mv"
#define FILESEP "/"
#else
#include <time.h>
#include <stdarg.h>
#include <io.h>
#define tempnam _tempnam
#define popen _popen
#define pclose _pclose
#define unlink _unlink
#define SYSTEM_COPY_COMMAND_s "copy"
#define SYSTEM_MOVE_COMMAND_s "move"
#define FILESEP "\\"
#endif

#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "ae/ae_errors.h"
#include "sa/am.h"
#include <tccore/aom.h>
#include <tccore/aom_prop.h>
#include <bom/bom.h>
#include <bom/bom_attr.h>
#include <bom/bom_msg.h>
#include <epm/cr.h>
#include <tccore/custom.h>
#include <ae/dataset.h>
#include <ae/dataset_msg.h>
#include <ecm/ecm.h>
#include <pom/enq/enq.h>
#include <tc/emh.h>
#include <tc/folder.h>
#include <tccore/grm.h>
#include <tccore/grmtype.h>
#include <sa/groupmember.h>
#include <ict/ict_userservice.h>
#include <tccore/item.h>
#include <tccore/item_errors.h>
#include <tccore/item_msg.h>
#include <lov/lov.h>
#include <lov/lov_errors.h>
#include <me/me.h>
#include <base_utils/Mem.h>
#include <tccore/method.h>
#include <pom/pom/pom.h>
#include <property/prop.h>
#include <tc/preferences.h>
#include <qry/qry.h>
#include <sa/role.h>
#include <sa/sa.h>
#include <fclasses/tc_date.h>
#include <tccore/tctype.h>
#include <fclasses/tc_stdlib.h>
#include <fclasses/tc_string.h>
#include <ug_va_copy.h>
#include <user_exits/user_exits.h>
#include <tccore/workspaceobject.h>

#ifdef __cplusplus
extern "C" 
{
#endif

#define ITK(x, xx) { \
    char *err_string; \
    if ( ifail == ITK_ok ) \
    { \
        if( (ifail = (x)) != ITK_ok) \
        { \
            EMH_get_error_string (NULLTAG, ifail, &err_string); \
            TC_write_syslog ("ERROR: %d ERROR MSG: %s.\n", ifail, err_string); \
            TC_write_syslog ("FUNCTION: %s\nFILE: %s LINE: %d\n",#x, __FILE__, __LINE__); \
            printf( "ERROR: %d ERROR MSG: %s.\nFUNCTION: %s\nFILE: %s LINE: %d\n", ifail, err_string, #x, __FILE__, __LINE__ ); \
            if(err_string) MEM_free(err_string); \
            \
            if (xx == true) \
            { \
                ITK_exit_module(true); \
                exit( -1 ); \
            } \
        } \
    } \
    else \
    { \
        printf ( "Skipping Function call [%s] and return.\n", #x ); \
        return ifail; \
    } \
}

//extern DLLAPI int SYMC_BOMLine_add_method
//(
//	METHOD_message_t *method_msg_tag, va_list args
//);


/**
 * 팀센타에 M7_BOPActivity를 저장할 때 작동하는 함수
 */
extern DLLAPI int SYMC_save_post_method
(
    METHOD_message_t *method_msg_tag,   ///< (I) 정보
    va_list args                        ///< (I) 파라메터
);

extern DLLAPI int SYMC_check_me_consumed(tag_t bopocc_puid_tag, tag_t *node_tag);
/**
 * BOP 하위 MEConsumed로 연결된 BOP의 Occurrence PUID로 EBOM에서의 SupplyMode 값을 조회하는 함수
 */
//extern DLLAPI int SYMC_get_SModeValue_FromEBOM
//(
//    tag_t bopocc_puid_tag,	///< (I) BOP Occurrence PUID Tag
//    char **str_smode		///< (O) Supply Mode value of EBOM
//);

/**
 * Activity를 생성할 때, 공법의 MECO를 가져와서 설정하는 함수
 */
//extern DLLAPI int SYMC_activity_crete_post_method
//(
//    METHOD_message_t *method_msg_tag,   ///< (I) 정보
//    va_list args                        ///< (I) 파라메터
//);

/**
 * Activity를 수정할 때, 공법의 MECO를 가져와서 설정하는 함수
 */
//extern DLLAPI int SYMC_set_activity_meco_no
//(
//    tag_t activity_tag   ///< (I) 정보
//);

#ifdef __cplusplus
}
#endif 

#endif
