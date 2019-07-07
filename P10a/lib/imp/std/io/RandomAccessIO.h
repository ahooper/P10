#ifndef STD_IO_RANDOMACCESSIO_P_H
#define STD_IO_RANDOMACCESSIO_P_H
// source_filename = "std/io/RandomAccessIO.p" ; 2019-03-10T15:07:09.758
// referenced object types
typedef struct obj_std_lang_Array obj_std_lang_Array;
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
typedef struct obj_std_lang_String obj_std_lang_String;
typedef struct obj_std_lang_System obj_std_lang_System;
typedef struct obj_std_lang_Throw obj_std_lang_Throw;
typedef struct obj_std_io_RandomAccessIO obj_std_io_RandomAccessIO;
typedef struct obj_std_io_FileWriter obj_std_io_FileWriter;
typedef struct obj_std_io_BufferedWriter obj_std_io_BufferedWriter;
typedef struct obj_std_io_Reader obj_std_io_Reader;
typedef struct obj_std_io_Writer obj_std_io_Writer;
// class structures
struct mtab_std_io_RandomAccessIO {
    obj_std_lang_Class* _class;
    obj_std_lang_Class* (*getClass)(obj_std_lang_Object*); //1 public getClass[]
    void (*close)(obj_std_io_RandomAccessIO*); //2 public close[]
    int32_t (*read_Pbyte_Y_Pint_Pint)(obj_std_io_RandomAccessIO*,array_int8_t*,int32_t,int32_t); //3 public read[byte[], int, int]
    int32_t (*write_Pbyte_Y_Pint_Pint)(obj_std_io_RandomAccessIO*,array_int8_t*,int32_t,int32_t); //4 public write[byte[], int, int]
    void (*skip_Pint)(obj_std_io_RandomAccessIO*,int32_t); //5 public skip[int]
    int64_t (*getPointer)(obj_std_io_RandomAccessIO*); //6 public getPointer[]
    void (*seek_Plong)(obj_std_io_RandomAccessIO*,int64_t); //7 public seek[long]
    int64_t (*length)(obj_std_io_RandomAccessIO*); //8 public length[]
    void (*setLength_Plong)(obj_std_io_RandomAccessIO*,int64_t); //9 public setLength[long]
    int32_t (*read_Pbyte_Y)(obj_std_io_RandomAccessIO*,array_int8_t*); //10 public read[byte[]]
    int32_t (*write_Pbyte_Y)(obj_std_io_RandomAccessIO*,array_int8_t*); //11 public write[byte[]]
};
struct obj_std_io_RandomAccessIO {
    struct mtab_std_io_RandomAccessIO* _mtt;
    int32_t fileRef; //1 private fileRef
};
typedef struct {
    struct mtab_std_lang_Array* _mtt;
    int32_t length;
    obj_std_io_RandomAccessIO* elements[0];
} array_obj_std_io_RandomAccessIO;
// static fields
// static methods
extern void std_io_RandomAccessIO__class_initialize();
// referenced classes
#include "std/io/BufferedWriter.h"
#include "std/lang/Object.h"
#include "std/lang/System.h"
#include "std/io/Reader.h"
#include "std/lang/Array.h"
#include "std/io/FileWriter.h"
#include "std/lang/Class.h"
#include "std/lang/String.h"
#include "std/io/Writer.h"
#include "std/lang/Throw.h"
extern void std_io_RandomAccessIO_close(obj_std_io_RandomAccessIO*);
extern int32_t std_io_RandomAccessIO_read_Pbyte_Y_Pint_Pint(obj_std_io_RandomAccessIO*,array_int8_t*,int32_t,int32_t);
extern int32_t std_io_RandomAccessIO_write_Pbyte_Y_Pint_Pint(obj_std_io_RandomAccessIO*,array_int8_t*,int32_t,int32_t);
extern void std_io_RandomAccessIO_skip_Pint(obj_std_io_RandomAccessIO*,int32_t);
extern int64_t std_io_RandomAccessIO_getPointer(obj_std_io_RandomAccessIO*);
extern void std_io_RandomAccessIO_seek_Plong(obj_std_io_RandomAccessIO*,int64_t);
extern int64_t std_io_RandomAccessIO_length(obj_std_io_RandomAccessIO*);
extern void std_io_RandomAccessIO_setLength_Plong(obj_std_io_RandomAccessIO*,int64_t);
#endif /* STD_IO_RANDOMACCESSIO_P_H */
