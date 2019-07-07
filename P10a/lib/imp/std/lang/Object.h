#ifndef STD_LANG_OBJECT_P_H
#define STD_LANG_OBJECT_P_H
// source_filename = "std/lang/Object.p" ; 2019-03-12T14:17:44.942
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_System obj_std_lang_System;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_std_io_FileWriter obj_std_io_FileWriter;
typedef struct obj_std_io_BufferedWriter obj_std_io_BufferedWriter;
typedef struct obj_std_io_Reader obj_std_io_Reader;
typedef struct obj_std_io_Writer obj_std_io_Writer;
// class structures
struct mtab_std_lang_Object {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
};
struct obj_std_lang_Object {
    struct mtab_std_lang_Object* _mtt;
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_std_lang_Object* elements[0];
} array_obj_std_lang_Object;
// static fields
// static methods
extern void std_lang_Object__class_initialize();
// referenced classes
#include "std/lang/Array.h"
#include "std/io/Reader.h"
#include "std/lang/Class.h"
#include "std/lang/System.h"
#include "std/io/BufferedWriter.h"
#include "std/lang/String.h"
#include "std/io/Writer.h"
#include "std/lang/Throw.h"
#include "std/io/FileWriter.h"
extern obj_std_lang_Class* std_lang_Object_getClass(obj_std_lang_Object*);
#endif /* STD_LANG_OBJECT_P_H */
