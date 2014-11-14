//
//  BNMultisigScriptPubKey.h
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNScriptPubKey.h"

@interface BNMultisigScriptPubKey : BNScriptPubKey

@property NSMutableArray *pubKeys;

@end
