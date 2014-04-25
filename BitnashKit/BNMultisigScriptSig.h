//
//  BNMultiScriptSig.h
//  BitnashKit
//
//  Created by Rich Collins on 3/25/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNScriptSig.h"

@interface BNMultisigScriptSig : BNScriptSig

@property NSArray *signatures;

@end
