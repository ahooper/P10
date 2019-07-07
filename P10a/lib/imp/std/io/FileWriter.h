#ifndef STD_IO_FILEWRITER_P_H
#define STD_IO_FILEWRITER_P_H
// source_filename = "std/io/FileWriter.p" ; 2019-03-12T14:17:44.985
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_System obj_std_lang_System;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_std_io_FileWriter obj_std_io_FileWriter;
typedef struct obj_std_io_Writer obj_std_io_Writer;
typedef struct obj_std_io_BufferedWriter obj_std_io_BufferedWriter;
typedef struct obj_std_io_Reader obj_std_io_Reader;
// class structures
struct mtab_std_io_FileWriter {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
    void (*close)(obj_std_io_FileWriter*); //2 public close[]
    int32_t (*write_Pbyte_Y_Pint_Pint)(obj_std_io_FileWriter*,array_int8_t*,int32_t,int32_t); //3 public write[byte[], int, int]
    int32_t (*write_Pbyte_Y)(obj_std_io_Writer*,array_int8_t*); //4 public write[byte[]]
    int32_t (*write_Pbyte_Y_Pint_Pint_Pbyte_Y_Pint_Pint)(obj_std_io_FileWriter*,array_int8_t*,int32_t,int32_t,array_int8_t*,int32_t,int32_t); //5 public write[byte[], int, int, byte[], int, int]
    void (*new_Pint)(obj_std_io_FileWriter*,int32_t); //6 public new[int]
};
struct obj_std_io_FileWriter {
    struct mtab_std_io_FileWriter* _mtt;
    int32_t fileRef; //1 private fileRef
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_std_io_FileWriter* elements[0];
} array_obj_std_io_FileWriter;
// static fields
// static methods
extern obj_std_io_FileWriter* std_io_FileWriter___newInstance_Pint(int32_t);
extern void std_io_FileWriter__class_initialize();
// referenced classes
#include "std/io/BufferedWriter.h"
#include "std/lang/Array.h"
#include "std/lang/Throw.h"
#include "std/lang/String.h"
#include "std/lang/Class.h"
#include "std/lang/System.h"
#include "std/lang/Object.h"
#include "std/io/Reader.h"
#include "std/io/Writer.h"
extern void std_io_FileWriter_close(obj_std_io_FileWriter*);
extern int32_t std_io_FileWriter_write_Pbyte_Y_Pint_Pint(obj_std_io_FileWriter*,array_int8_t*,int32_t,int32_t);
extern int32_t std_io_FileWriter_write_Pbyte_Y_Pint_Pint_Pbyte_Y_Pint_Pint(obj_std_io_FileWriter*,array_int8_t*,int32_t,int32_t,array_int8_t*,int32_t,int32_t);
#endif /* STD_IO_FILEWRITER_P_H */
