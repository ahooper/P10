#include "native/p10a.h"
#include "std/lang/Object.h"
#include "std/lang/Class.h"

obj_std_lang_Class* std_lang_Object_getClass(obj_std_lang_Object* this) {
	return this->_mtt->_class;
}
	
