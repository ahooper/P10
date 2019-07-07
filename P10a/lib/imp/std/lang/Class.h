#ifndef STD_LANG_CLASS_P_H
#define STD_LANG_CLASS_P_H
// source_filename = "std/lang/Class.p" ; 2019-03-10T15:07:06.342
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
// class structures
struct mtab_std_lang_Class {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
    obj_std_lang_String* (*getName)(obj_std_lang_Class*); //2 public getName[]
};
struct obj_std_lang_Class {
    struct mtab_std_lang_Class* _mtt;
    obj_std_lang_String* name; //1 private name
    int32_t instanceSize; //2 private instanceSize
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_std_lang_Class* elements[0];
} array_obj_std_lang_Class;
// static fields
// static methods
extern void std_lang_Class__class_initialize();
// referenced classes
#include "std/lang/Array.h"
#include "std/lang/Throw.h"
#include "std/lang/Object.h"
#include "std/lang/String.h"
#endif /* STD_LANG_CLASS_P_H */
