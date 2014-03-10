//
//  BNTxIn.h
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNScriptSig.h"

@interface BNTxIn : NSObject

@property BNScriptSig *scriptSig;
@property NSString *previousTxHash;
@property NSNumber *previousOutIndex;
@property NSNumber *previousOutValue;

@end
