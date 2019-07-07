#ifndef TEST_VARIOUS_P_H
#define TEST_VARIOUS_P_H
// source_filename = "test/Various.p" ; 2019-02-02T09:46:30.858
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_Various obj_Various;
typedef struct obj_std_lang_StdIO obj_std_lang_StdIO;
#include "std/lang/StdIO.h"
#include "std/lang/Throw.h"
#include "std/lang/String.h"
#include "std/lang/Array.h"
#include "std/lang/Object.h"
#include "std/lang/Class.h"
struct mtab_Various {
    obj_std_lang_Class* _class;
    void (*f_Pint)(obj_Various*,int32_t); //1 public f[int]
    void (*g)(obj_Various*); //2 public g[]
    int32_t (*frotz_PString_Y)(obj_Various*,array_obj_std_lang_String**); //3 public frotz[String[]]
    int32_t (*index_Pchar)(obj_Various*,int32_t); //4 public index[char]
    void (*trySwitch_Pchar)(obj_Various*,int32_t); //5 public trySwitch[char]
};
struct obj_Various {
    struct mtab_Various* _mtt;
    int32_t n; //1 public n
    array_int32_t* content; //2 public content
    int32_t offset; //3 public offset
    int32_t length; //4 public length
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_Various* elements[0];
} array_Various;
extern int32_t Various_serial;
extern int32_t Various_factorial_Pint(int32_t);
