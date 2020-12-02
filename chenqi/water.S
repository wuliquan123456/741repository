#define GPIOEOUT 		0xC001E000
#define GPIOEOUTENB 	0xC001E004
#define GPIOEALTFN0 	0xC001E020
#define GPIOEALTFN1 	0xC001E024

#define GPIOCOUT 		0xC001C000
#define GPIOCOUTENB 	0xC001C004
#define GPIOCALTFN0 	0xC001C020
#define GPIOCALTFN1 	0xC001C024

.text	//代码段的开始
.global _start

_start:
    //配置GPIOE13为输出模式
	//GPIOEALTFN0 &= ~(3<<26); 
	//GPIOEOUTENB |= 1<<13; 
    LDR r1,=GPIOEALTFN0

    LDR r0,[r1]          //取寄存器的值
    LDR r2,=~(3<<26);
    AND r0,r0,r2;        //r0=r0&r2
    STR r0,[r1]          //修改GPIOEALTFN0 设置IO

    LDR r1,=GPIOEOUTENB

    LDR r0,[r1]          //取寄存器的值
    LDR r2,=(1<<13);
    ORR r0,r0,r2;        //r0=r0|r2
    STR r0,[r1]          //修改GPIOEOUTENB 设置输出模式


	//配置GPIOC17为输出模式
	//GPIOCALTFN1 &= ~(3<<2);
	//GPIOCALTFN1 |= 1<<2;
	//GPIOCOUTENB |= 1<<17;
    LDR r1,=GPIOCALTFN1

    LDR r0,[r1]          //取寄存器的值
    LDR r2,=~(3<<2);
    AND r0,r0,r2;        //r0=r0&r2
    STR r0,[r1]          //修改GPIOCALTFN1的值

    LDR r1,=GPIOCALTFN1
    LDR r0,[r1]          //重新取寄存器的值
    LDR r2,=(1<<2);
    ORR r0,r0,r2;        //r0=r0&r2
    STR r0,[r1]          //再次修改GPIOCALTFN1的值

    LDR r1,=GPIOCOUTENB
    LDR r0,[r1]          //取寄存器的值
    LDR r2,=(1<<17);
    ORR r0,r0,r2;        //r0=r0|r2
    STR r0,[r1]          //修改GPIOEOUTENB的值


    //配置GPIOC7、GPIOC8为输出模式 
	//GPIOCALTFN0 &= ~((3<<16)|(3<<14)); 
	//GPIOCALTFN0 |= (1<<16)|(1<<14);
	//GPIOCOUTENB |= (1<<8)|(1<<7);
    LDR r1,=GPIOCALTFN0
    LDR r0,[r1]          //取寄存器值
    LDR r2,=~(3<<16);
    LDR r3,=~(3<<14);
    AND r2,r2,r3;
    AND r0,r0,r2;        
    STR r0,[r1]

    LDR r1,=GPIOCALTFN0
    LDR r0,[r1]          //取寄存器值
    LDR r2,=(1<<16);
    LDR r3,=(1<<14);
    ORR r2,r2,r3;        //r2= (1<<16)|(1<<14);
    ORR r0,r0,r2;        //r0=r0|r2
    STR r0,[r1]

    LDR r1,=GPIOCOUTENB
    LDR r0,[r1]          //取寄存器值
    LDR r2,=(1<<8);
    LDR r3,=(1<<7);
    ORR r2,r2,r3;        //r2= (1<<8)|(1<<7);
    ORR r0,r0,r2;        //r0=r0|r2
    STR r0,[r1]

loop:

	//点亮D7,其他灯熄灭
	//GPIOEOUT &= ~(1<<13);
    //GPIOCOUT |= 1<<17;
	//GPIOCOUT |= 1<<8;
	//GPIOCOUT |= 1<<7;
	LDR r1,=GPIOEOUT

	LDR r0,[r1]	        //取寄存器值
	LDR r2,=~(1<<13);   //r2=~(1<<13)
	AND r0,r0,r2;	    //r0=r0&r2 
	STR r0,[r1]		    //修改GPIOEOUT的值

    LDR r1,=GPIOEOUT
	LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<17);    //r2=(1<<17)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值	

    LDR r1,=GPIOEOUT
    LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<8);     //r2=(1<<8)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值	

    LDR r1,=GPIOEOUT
    LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<7);     //r2=(1<<7)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值	

    bl  delay		//delay

    //点亮D8,其他灯熄灭
	//GPIOEOUT |= 1<<13;
	//GPIOCOUT &= ~(1<<17);
	//GPIOCOUT |= 1<<8;
	//GPIOCOUT |= 1<<7;
	LDR r1,=GPIOEOUT
	LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<13);    //r2=(1<<13)
	ORR r0,r0,r2;	    //r0=r0&r2 
	STR r0,[r1]		    //修改GPIOEOUT的值

    LDR r1,=GPIOEOUT
	LDR r0,[r1]	        //取寄存器值
	LDR r2,=~(1<<17);    //r2=~(1<<17)
	AND r0,r0,r2;	    //r0=r0&r2 
	STR r0,[r1]		    //修改GPIOEOUT的值	

    LDR r1,=GPIOEOUT
    LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<8);     //r2=(1<<8)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值	

    LDR r1,=GPIOEOUT
    LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<7);     //r2=(1<<7)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值	


    bl  delay		//delay

    //点亮D9,其他灯熄灭
    //GPIOEOUT |= 1<<13;
	//GPIOCOUT |= 1<<17;
	//GPIOCOUT &= ~(1<<8);
	//GPIOCOUT |= 1<<7;
	LDR r1,=GPIOEOUT
	LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<13);    //r2=(1<<13)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值

 	LDR r1,=GPIOEOUT
	LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<17);    //r2=(1<<17)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值	

	LDR r1,=GPIOEOUT
    LDR r0,[r1]	        //取寄存器值
	LDR r2,=~(1<<8);     //r2=~(1<<8)
	AND r0,r0,r2;	    //r0=r0&r2 
	STR r0,[r1]		    //修改GPIOEOUT的值

	LDR r1,=GPIOEOUT
    LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<7);     //r2=(1<<7)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值	

    bl  delay		//delay

	//点亮D10,其他灯熄灭
	//GPIOEOUT |= 1<<13;
	//GPIOCOUT |= 1<<17;
	//GPIOCOUT |= 1<<8;
	//GPIOCOUT &= ~(1<<7);
	LDR r1,=GPIOEOUT
	LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<13);    //r2=(1<<13)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值

 	LDR r1,=GPIOEOUT
	LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<17);    //r2=(1<<17)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值

	LDR r1,=GPIOEOUT
    LDR r0,[r1]	        //取寄存器值
	LDR r2,=(1<<8);     //r2=(1<<8)
	ORR r0,r0,r2;	    //r0=r0|r2 
	STR r0,[r1]		    //修改GPIOEOUT的值	

	LDR r1,=GPIOEOUT
    LDR r0,[r1]	        //取寄存器值
	LDR r2,=~(1<<7);     //r2=~(1<<7)
	AND r0,r0,r2;	    //r0=r0&r2 
	STR r0,[r1]		    //修改GPIOEOUT的值


    bl  delay		//delay

    b loop

	
delay:
	push {r0,lr}
    ldr r0,=0x2FF0000 
	mov r0,#0x20000000
	
delay_loop:
	sub r0,r0,#1	//r0=r0-1
	cmp r0,#0		//判断r0是否为0
	bne delay_loop	//若r0！=0，则跳转到delay_loop
	bx lr			//否则函数返回
		


.end	//代码段的结束


