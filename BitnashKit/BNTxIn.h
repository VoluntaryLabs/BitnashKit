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
@property NSNumber *previousOutIndex;
@property NSString *previousTxSerializedHex;
@property NSString *previousTxHash;

@end
