#ifndef TEST_OVERRIDE_P_H
#define TEST_OVERRIDE_P_H
// source_filename = "test/Override.p" ; 2019-02-04T13:07:56.281
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_test_Override obj_test_Override;
typedef struct obj_std_lang_TempIO obj_std_lang_TempIO;
#include "std/lang/Object.h"
#include "std/lang/Class.h"
#include "std/lang/Throw.h"
#include "std/lang/Array.h"
#include "std/lang/TempIO.h"
#include "std/lang/String.h"
struct mtab_test_Override {
    obj_std_lang_Class* _class;
    void (*dummy)(obj_test_Override*); //1 public dummy[]
    void (*add)(obj_test_Override*); //2 public add[]
};
struct obj_test_Override {
    struct mtab_test_Override* _mtt;
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_test_Override* elements[0];
} array_test_Override;
#endif /* TEST_OVERRIDE_P_H */
