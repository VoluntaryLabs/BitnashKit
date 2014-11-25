//
//  BNPayToAddressScriptSig.h
//  BitnashKit
//
//  Created by Rich Collins on 3/25/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import <BitnashKit/BitnashKit.h>
#import "BNObject.h"

@interface BNPayToAddressScriptSig : BNObject

@property NSString *pubKey;
@property NSString *signature;

@end
