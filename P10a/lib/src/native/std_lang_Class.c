#include "native/p10a.h"
#include "std/lang/Object.h"
#include "std/lang/Class.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
/*
typedef struct obj_std_lang_Class obj_std_lang_Class;
typedef struct obj_std_lang_Object obj_std_lang_Object;
struct obj_std_lang_Object {
	struct mtab_std_lang_Object* _mtt;
};
struct obj_std_lang_Class {
	struct mtab_std_lang_Class* _mtt;
	obj_std_lang_String* name; //1 private name
	int32_t instanceSize; //2 private instanceSize
};
*/

obj_std_lang_Object* std_lang_Class_newInstance(obj_std_lang_Class* class, struct mtab_std_lang_Object* _mtt) {
	//*DEBUG*/fprintf(stderr,"std_lang_Class_newInstance %*s size %d\n", class->name->count, class->name->value->elements+class->name->offset, class->instanceSize);
	obj_std_lang_Object* n = calloc(class->instanceSize,1);
	n->_mtt = _mtt;
	return n;
}
