#ifndef STD_LANG_STRING_P_H
#define STD_LANG_STRING_P_H
// source_filename = "std/lang/String.p" ; 2019-03-10T15:07:06.828
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
// class structures
struct mtab_std_lang_String {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
    void (*new)(obj_std_lang_String*); //2 public new[]
    void (*new_PString)(obj_std_lang_String*,obj_std_lang_String*); //3 public new[String]
    void (*new_Pbyte_Y_Pint_Pint)(obj_std_lang_String*,array_int8_t*,int32_t,int32_t); //4 public new[byte[], int, int]
    void (*new_Pbyte_Y)(obj_std_lang_String*,array_int8_t*); //5 public new[byte[]]
    int32_t (*byteSize)(obj_std_lang_String*); //6 public byteSize[]
    int8_t (*byteAt_Pint)(obj_std_lang_String*,int32_t); //7 public byteAt[int]
    int32_t (*charSize_Pchar)(obj_std_lang_String*,int32_t); //8 public charSize[char]
    int32_t (*charCount)(obj_std_lang_String*); //9 public charCount[]
    int32_t (*charAt_Pint)(obj_std_lang_String*,int32_t); //10 public charAt[int]
    int1_t (*equals_PString)(obj_std_lang_String*,obj_std_lang_String*); //11 public equals[String]
    int1_t (*isEmpty)(obj_std_lang_String*); //12 public isEmpty[]
    int32_t (*indexOf_PString)(obj_std_lang_String*,obj_std_lang_String*); //13 public indexOf[String]
    int32_t (*indexOf_PString_Pint)(obj_std_lang_String*,obj_std_lang_String*,int32_t); //14 public indexOf[String, int]
    int32_t (*indexOf_Pchar)(obj_std_lang_String*,int32_t); //15 public indexOf[char]
    int32_t (*indexOf_Pchar_Pint)(obj_std_lang_String*,int32_t,int32_t); //16 public indexOf[char, int]
    int32_t (*lastIndexOf_PString)(obj_std_lang_String*,obj_std_lang_String*); //17 public lastIndexOf[String]
    int32_t (*lastIndexOf_PString_Pint)(obj_std_lang_String*,obj_std_lang_String*,int32_t); //18 public lastIndexOf[String, int]
    int32_t (*lastIndexOf_Pchar)(obj_std_lang_String*,int32_t); //19 public lastIndexOf[char]
    int32_t (*lastIndexOf_Pchar_Pint)(obj_std_lang_String*,int32_t,int32_t); //20 public lastIndexOf[char, int]
    int1_t (*regionMatches_Pint_PString_Pint_Pint)(obj_std_lang_String*,int32_t,obj_std_lang_String*,int32_t,int32_t); //21 public regionMatches[int, String, int, int]
    int1_t (*startsWith_PString_Pint)(obj_std_lang_String*,obj_std_lang_String*,int32_t); //22 public startsWith[String, int]
    int1_t (*startsWith_PString)(obj_std_lang_String*,obj_std_lang_String*); //23 public startsWith[String]
    int1_t (*endsWith_PString)(obj_std_lang_String*,obj_std_lang_String*); //24 public endsWith[String]
    obj_std_lang_String* (*substring_Pint_Pint)(obj_std_lang_String*,int32_t,int32_t); //25 public substring[int, int]
    obj_std_lang_String* (*substring_Pint)(obj_std_lang_String*,int32_t); //26 public substring[int]
};
struct obj_std_lang_String {
    struct mtab_std_lang_String* _mtt;
    array_int8_t* value; //1 private value
    int32_t count; //2 private count
    int32_t offset; //3 private offset
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_std_lang_String* elements[0];
} array_obj_std_lang_String;
// static fields
extern obj_std_lang_String* std_lang_String_EMPTY;
// static methods
extern obj_std_lang_String* std_lang_String___newInstance_Pbyte_Y(array_int8_t*);
extern obj_std_lang_String* std_lang_String___newInstance_Pbyte_Y_Pint_Pint(array_int8_t*,int32_t,int32_t);
extern obj_std_lang_String* std_lang_String___newInstance_PString(obj_std_lang_String*);
extern obj_std_lang_String* std_lang_String___newInstance();
extern void std_lang_String_unitTest();
extern void std_lang_String__class_initialize();
// referenced classes
#include "std/lang/Throw.h"
#include "std/lang/Class.h"
#include "std/lang/Array.h"
#include "std/lang/Object.h"
#endif /* STD_LANG_STRING_P_H */
