#ifndef STD_TEXT_FORMATTER_P_H
#define STD_TEXT_FORMATTER_P_H
// source_filename = "std/text/Formatter.p" ; 2019-03-10T15:07:11.199
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_System obj_std_lang_System;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_std_text_Formatter obj_std_text_Formatter;
typedef struct obj_std_io_BufferedWriter obj_std_io_BufferedWriter;
typedef struct obj_std_io_FileWriter obj_std_io_FileWriter;
typedef struct obj_std_io_Reader obj_std_io_Reader;
typedef struct obj_std_io_Writer obj_std_io_Writer;
// class structures
struct mtab_std_text_Formatter {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
    void (*new_PBufferedWriter)(obj_std_text_Formatter*,obj_std_io_BufferedWriter*); //2 public new[BufferedWriter]
    obj_std_text_Formatter* (*c_Pchar)(obj_std_text_Formatter*,int32_t); //3 public c[char]
    obj_std_text_Formatter* (*s_PString_Pint)(obj_std_text_Formatter*,obj_std_lang_String*,int32_t); //4 public s[String, int]
    obj_std_text_Formatter* (*s_PString)(obj_std_text_Formatter*,obj_std_lang_String*); //5 public s[String]
    obj_std_text_Formatter* (*d_Pint_Pint)(obj_std_text_Formatter*,int32_t,int32_t); //6 public d[int, int]
    obj_std_text_Formatter* (*d_Pint)(obj_std_text_Formatter*,int32_t); //7 public d[int]
    obj_std_text_Formatter* (*d_Plong_Pint)(obj_std_text_Formatter*,int64_t,int32_t); //8 public d[long, int]
    obj_std_text_Formatter* (*d_Plong)(obj_std_text_Formatter*,int64_t); //9 public d[long]
    obj_std_text_Formatter* (*x_Pint_Pint)(obj_std_text_Formatter*,int32_t,int32_t); //10 public x[int, int]
    obj_std_text_Formatter* (*x_Pint)(obj_std_text_Formatter*,int32_t); //11 public x[int]
    obj_std_text_Formatter* (*x_Plong_Pint)(obj_std_text_Formatter*,int64_t,int32_t); //12 public x[long, int]
    obj_std_text_Formatter* (*x_Plong)(obj_std_text_Formatter*,int64_t); //13 public x[long]
};
struct obj_std_text_Formatter {
    struct mtab_std_text_Formatter* _mtt;
    obj_std_io_BufferedWriter* writer; //1 private writer
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_std_text_Formatter* elements[0];
} array_obj_std_text_Formatter;
// static fields
// static methods
extern obj_std_text_Formatter* std_text_Formatter___newInstance_PBufferedWriter(obj_std_io_BufferedWriter*);
extern void std_text_Formatter__class_initialize();
// referenced classes
#include "std/lang/String.h"
#include "std/lang/Array.h"
#include "std/lang/System.h"
#include "std/io/BufferedWriter.h"
#include "std/io/Reader.h"
#include "std/io/Writer.h"
#include "std/lang/Object.h"
#include "std/lang/Class.h"
#include "std/lang/Throw.h"
#include "std/io/FileWriter.h"
#endif /* STD_TEXT_FORMATTER_P_H */
