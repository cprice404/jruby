// Include the Ruby headers and goodies
#include "ruby.h"

// Defining a space for information and references about the module to be stored internally
VALUE MyTest = Qnil;

// Prototype for the initialization method - Ruby calls this, not you
void Init_mytest();

// Prototype for our method 'test1' - methods are prefixed by 'method_' here
VALUE method_test1(VALUE self);
VALUE method_block_given(VALUE self);
VALUE method_rb_yield(VALUE self);

// The initialization method for this module
void Init_mytest() {
	MyTest = rb_define_module("MyTest");
	rb_define_method(MyTest, "test1", method_test1, 0);
	rb_define_method(MyTest, "block_given", method_block_given, 0);
	rb_define_method(MyTest, "rb_yield", method_rb_yield, 0);
}

// Our 'test1' method.. it simply returns a value of '10' for now.
VALUE method_test1(VALUE self) {
	int x = 10;
	return INT2NUM(x);
}

VALUE method_block_given(VALUE self) {
   int retval = rb_block_given_p(); 
   if (retval) {
      printf("Block given %d\n", retval);
   } else {
      printf("No block given %d\n", retval);
   }
   return retval ? Qtrue : Qfalse;
}

VALUE method_rb_yield(VALUE self) {
   rb_yield(Qnil);
}

