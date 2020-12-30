
.extern printf
.global led_d7_toggle
.global do_irq
.global gic_dist_init
.global gic_cpu_init
.global s5p6818_irq_init
.global timer_init
.global led_init
#define  GICC_CTRL		 	0xC000A000
#define  GICC_PMR		 	0xC000A004
#define  GICC_EOIR		 	0xC000A010

#define	 GICC_IAR			0xC000A00C

#define  GICD_CTRL		 	0xC0009000
#define  GICD_ISCENABLER1	0xC0009104
#define  GICD_IPRIORITYR14	0xC0009438
#define  GICD_ITARGETSR14	0xC0009838
#define  GICD_ICFGR3		0xC0009C0C

#define  TINT_CSTAT			0xC0017044

#define GPIOAOUTENB 	0xC001A004
#define GPIOAALTFN0 	0xC001A020
#define GPIOAALTFN1 	0xC001A024
#define GPIOAPAD    	0xC001A018

#define GPIOBOUTENB 	0xC001B004
#define GPIOBALTFN0 	0xC001B020
#define GPIOBALTFN1 	0xC001B024
#define GPIOBPAD    	0xC001B018

#define GPIOEOUT 		0xC001E000
#define GPIOEOUTENB 	0xC001E004
#define GPIOEOUTALTFN0 	0xC001E020
#define GPIOEOUTALTFN1 	0xC001E024

#define  TCFG0		 	0xC0017000
#define  TCFG1		  	0xC0017004
#define  TCON		  	0xC0017008
#define  TCNTB1		  	0xC0017018


irq_cnt: 
	.int 0

WTCNT:
	.int 61035 
led_d7_toggle:
	push {r0-r12,lr}

	ldr r0,=GPIOEOUT		//获取寄存器GPIOEOUT的地址
	ldr r1,[r0]				//取出寄存器GPIOEOUT上的内容
	
	eor r1,r1,#1<<13	
	str r1,[r0]				//修改寄存器GPIOEOUT的值	
	pop	{r0-r12,lr}
	
	bx lr

do_irq:
	push {r0-r12,lr}
	
	//获取中断号
	ldr r0,=GICC_IAR
	ldr r1,[r0]
	ldr r2,=0x3FF
	and r1,r1,r2
	
	stmfd sp!,{r1}				//临时保存r1寄存器的数据到栈，当前保存的是中断号
	
	cmp r1,#56
	bne do_irq_end
	
	ldr r0,=GICD_ISCENABLER1
	ldr r1,[r0]
	orr r1,r1,#1<<24
	str r1,[r0]
	
	bl led_d7_toggle
	
	//喂狗
	ldr r0,=WTCNT
	ldr r1,=#61035
	str r1,[r0]
	
	ldr r0,=irq_cnt
	ldr r1,[r0]
	add r1,r1,#1
	str r1,[r0]
	
	ldr r0,=sz2
	bl printf
	
	//清空定时器1中断标志位
	ldr r0,=TINT_CSTAT
	ldr r1,[r0]
	orr r1,r1,#1<<6
	str r1,[r0]
	
do_irq_end:	
	//清除所有中断
	ldr r0,=GICC_EOIR
	ldr r1,[r0]
	ldr r2,=0x3FF
	bic r1,r1,r2
	
	ldmfd sp!,{r2}				//将栈保存的中断号恢复到r2寄存器
	
	orr r1,r1,r2
	str r1,[r0]	
	
	pop	{r0-r12,lr}
	bx lr
	

gic_dist_init:
	push {r0-r12,lr}
	
	//GICD_CTRL = 0x0;
	ldr r0,=GICC_CTRL
	mov r1,#0
	str r1,[r0]
	
	//GICD_ISENABLER1
	ldr r0,=GICD_ISCENABLER1
	ldr r1,[r0]
	orr r1,r1,#1<<24
	str r1,[r0]	
	
	//GICD_IPRIORITYR14
	ldr r0,=GICD_IPRIORITYR14
	ldr r1,[r0]
	bic r1,r1,#0xFF
	orr r1,r1,#56
	str r1,[r0]		
	
	//GICD_ITARGETSR14
	ldr r0,=GICD_ITARGETSR14
	ldr r1,[r0]
	bic r1,r1,#0xFF
	orr r1,r1,#0x01
	str r1,[r0]	

	//GICD_ICFGR3
	ldr r0,=GICD_ICFGR3
	ldr r1,[r0]
	bic r1,r1,#(0x03<<16)
	str r1,[r0]	

	//GICD_CTRL = 0x1;
	ldr r0,=GICC_CTRL
	mov r1,#1
	str r1,[r0]	

	pop	{r0-r12,lr}
	
	bx lr
	
gic_cpu_init:
	push {r0-r12,lr}
	
	//GICC_PMR 
	ldr r0,=GICC_PMR
	mov r1,#0xF0
	str r1,[r0]	
	
	//GICC_CTRL 
	ldr r0,=GICC_CTRL
	mov r1,#0x01
	str r1,[r0]		
	
	pop	{r0-r12,lr}
	bx lr


s5p6818_irq_init:
	push {r0-r12,lr}
	
	bl gic_dist_init
	bl gic_cpu_init
	
	pop	{r0-r12,lr}
	bx lr
	
	
timer_init:
	push {r0-r12,lr}

	//设置一级预分频值
	ldr r0,=TCFG0
	ldr r1,[r0]
	bic r1,#0xFF	//低8位清零
	orr r1,r1,#249
	str r1,[r0]

	//设置二级分频值
	ldr r0,=TCFG1
	ldr r1,[r0]
	bic r1,#0xF0	//[7:4]位清零
	orr r1,r1,#0x40
	str r1,[r0]	
	
	//设置计数值
	ldr r0,=37500
	ldr r1,=TCNTB1
	str r0,[r1]
	
	//打开手动加载
	ldr r0,=TCON
	ldr r1,[r0]
	orr r1,r1,#1<<9
	str r1,[r0]
	
	//关闭手动加载	
	ldr r0,=TCON
	ldr r1,[r0]
	bic r1,#1<<9
	str r1,[r0]	
	
	//打开自动加载	
	ldr r0,=TCON
	ldr r1,[r0]
	orr r1,#1<<11
	str r1,[r0]	

	
	
	//使能中断并清空标志位
	ldr r0,=TINT_CSTAT
	ldr r1,[r0]
	orr r1,r1,#(1<<6)|(1<<1)
	str r1,[r0]	

	//使能T1定时器	
	ldr r0,=TCON
	ldr r1,[r0]
	orr r1,#1<<8
	str r1,[r0]		
	
	pop	{r0-r12,lr}
	
	bx lr

led_init:

	push {r0-r12,lr}
	
	//GPIOEOUTALTFN0&=~(3<<26);
	ldr r0,=GPIOEOUTALTFN0	//获取寄存器GPIOEOUTALTFN0的地址
	ldr r1,[r0]				//取出寄存器GPIOEOUTALTFN0上的内容
	ldr r2,=~(3<<26)
	
	and r1,r1,r2			//r1 = r1 & r2
	str r1,[r0]				//修改寄存器GPIOEOUTALTFN0的值

	//GPIOEOUTENB|=1<<13;	
	ldr r0,=GPIOEOUTENB		//获取寄存器GPIOEOUTENB的地址
	ldr r1,[r0]				//取出寄存器GPIOEOUTENB上的内容
	ldr r2,=1<<13
	
	orr r1,r1,r2			//r1 = r1 | r2
	str r1,[r0]				//修改寄存器GPIOEOUTENB的值

	pop	{r0-r12,lr}
	bx lr


	//GPIOEOUT&=~(1<<13);	
	ldr r0,=GPIOEOUT		//获取寄存器GPIOEOUT的地址
	ldr r1,[r0]				//取出寄存器GPIOEOUT上的内容
	bic r1,#(1<<13)

	str r1,[r0]				//修改寄存器GPIOEOUT的值	
	
	
.macro irq_save_regs
	sub	sp, sp, #72
	stmia sp, {r0 - r12}
	add	r8, sp, #60
	stmdb r8, {sp, lr}^
	str	lr, [r8, #0]
	mrs	r6, spsr
	str	r6, [r8, #4]
	str	r0, [r8, #8]
	mov	r0, sp
.endm

.macro irq_restore_regs
	ldmia sp, {r0 - lr}^
	mov	r0, r0
	ldr	lr, [sp, #60]
	add	sp, sp, #72
	subs pc, lr, #4
.endm

/*
 * Exception vector table
 */
.text
	.arm

	.global	_start
_start:
	b reset
	ldr pc, _undefined_instruction
	ldr pc, _software_interrupt
	ldr pc, _prefetch_abort
	ldr pc, _data_abort
	ldr pc, _not_used
	ldr pc, _irq
	ldr pc, _fiq

_undefined_instruction:
	.long undefined_instruction
_software_interrupt:
	.long software_interrupt
_prefetch_abort:
	.long prefetch_abort
_data_abort:
	.long data_abort
_not_used:
	.long not_used
_irq:
	.long irq
_fiq:
	.long fiq

	.align 4
/* Magic number(16bytes) - xbootmagicnumber */
	.byte 0x78, 0x62, 0x6f, 0x6f, 0x74, 0x6d, 0x61, 0x67, 0x69, 0x63, 0x6e, 0x75, 0x6d, 0x62, 0x65, 0x72
/* Image start and image end information */
	.long __image_start, __image_end
/* Where the image was linked address */
	.long _start
/* Image sha256 digest */
	.long 0, 0, 0, 0
/* Magic number(16bytes) - xbootmagicnumber */
	.byte 0x78, 0x62, 0x6f, 0x6f, 0x74, 0x6d, 0x61, 0x67, 0x69, 0x63, 0x6e, 0x75, 0x6d, 0x62, 0x65, 0x72

/*
 * The actual reset code
 */
reset:
	ldr r0, =0xC0019000
	mov r1, #0
	str r1, [r0]

	/* Set the cpu to svc32 mode */
	mrs r0, cpsr
	bic r0, r0, #0x1f
	orr r0, r0, #0xd3
	msr cpsr, r0

	/* Enable NEON/VFP unit */
	mrc p15, #0, r1, c1, c0, #2
	orr r1, r1, #(0xf << 20)
	mcr p15, #0, r1, c1, c0, #2
	mov r1, #0
	mcr p15, #0, r1, c7, c5, #4
	mov r0, #0x40000000
	fmxr fpexc, r0

	/* Cache init */
	mrc	p15, 0, r0, c0, c0, 0
	and	r1, r0, #0x00f00000
	and	r2, r0, #0x0000000f
	orr r2, r2, r1, lsr #20-4
	cmp r2, #0x30
	mrceq p15, 0, r0, c1, c0, 1
	orreq r0, r0, #0x6
	mcreq p15, 0, r0, c1, c0, 1

	/* Invalidate L1 I/D */
	mov r0, #0
	mcr	p15, 0, r0, c8, c7, 0
	mcr	p15, 0, r0, c7, c5, 0

	/* Disable mmu stuff and caches */
	mrc	p15, 0, r0, c1, c0, 0
	bic	r0, r0, #0x00002000	@ clear bits 13 (--V-)
	bic	r0, r0, #0x00000007	@ clear bits 2:0 (-CAM)
	orr	r0, r0, #0x00001000	@ set bit 12 (---I) Icache
	orr	r0, r0, #0x00000002	@ set bit 1  (--A-) Align
	orr	r0, r0, #0x00000800	@ set bit 11 (Z---) BTB
	mcr	p15, 0, r0, c1, c0, 0

	/* Set Vector Base Address Register */
	ldr r0, =_start
	mcr p15, 0, r0, c12, c0, 0
	mrc p15, 0, r0, c1, c0, 0
	bic r0, #(1<<13)
	mcr p15, 0, r0, c1, c0, 0

	/* Initialize stacks */
	mrs r0, cpsr
	bic r0, r0, #0xdf
	orr r1, r0, #0x1b
	msr cpsr_cxsf, r1
	ldr sp, _stack_und_end

	bic r0, r0, #0xdf
	orr r1, r0, #0x17
	msr cpsr_cxsf, r1
	ldr sp, _stack_abt_end

	bic r0, r0, #0xdf
	orr r1, r0, #0x12
	msr cpsr_cxsf, r1
	ldr sp, _stack_irq_end

	bic r0, r0, #0xdf
	orr r1, r0, #0x11
	msr cpsr_cxsf, r1
	ldr sp, _stack_fiq_end

	bic r0, r0, #0xdf
	orr r1, r0, #0x13
	msr cpsr_cxsf, r1
	ldr sp, _stack_srv_end

	/* Copyself to link address */
	adr r0, _start
	ldr r1, =_start
	cmp r0, r1
	beq 1f
	ldr r0, _image_start
	adr r1, _start
	ldr r2, _image_end
	sub r2, r2, r0
	bl memcpy
1:	nop

	/* Copy data section */
	ldr r0, _data_start
	ldr r3, _image_start
	ldr r1, _data_shadow_start
	sub r1, r1, r3
	adr r3, _start
	add r1, r1 ,r3
	ldr r2, _data_shadow_start
	ldr r3, _data_shadow_end
	sub r2, r3, r2
	bl memcpy

	/* Clear bss section */
	ldr r0, _bss_start
	ldr r2, _bss_end
	sub r2, r2, r0
	mov r1, #0
	bl memset

	/* Call _main */
	ldr r1, =_main
	mov pc, r1
_main:
	ldr r0,=sz1
	bl printf
	
	bl led_init
	bl s5p6818_irq_init
	bl timer_init
	
_main_loop:
	bl do_irq
	b _main_loop
	
	
sz1:
	.asciz	"timer irq start\r\n"

sz2:
	.asciz	"timer irq %04d\r\n"	

/*
 * Exception handlers
 */
	.align 5
undefined_instruction:
	b	.

	.align 5
software_interrupt:
	b	.

	.align 5
prefetch_abort:
	b	.

	.align 5
data_abort:
	b	.

	.align 5
not_used:
	b	.

	.align 5
	.global irq
irq:
	ldr	sp, _stack_irq_end
	irq_save_regs
	.weak do_irq
	bl do_irq
	irq_restore_regs

	.align 5
fiq:
	ldr	sp, _stack_fiq_end
	irq_save_regs
	.weak do_fiq
	bl do_fiq
	irq_restore_regs

/*
 * The location of section
 */
 	.align 4
_image_start:
	.long __image_start
_image_end:
	.long __image_end
_data_shadow_start:
	.long __data_shadow_start
_data_shadow_end:
	.long __data_shadow_end
_data_start:
	.long __data_start
_data_end:
	.long __data_end
_bss_start:
	.long __bss_start
_bss_end:
	.long __bss_end
_stack_und_end:
	.long __stack_und_end
_stack_abt_end:
	.long __stack_abt_end
_stack_irq_end:
	.long __stack_irq_end
_stack_fiq_end:
	.long __stack_fiq_end
_stack_srv_end:
	.long __stack_srv_end
