//
//  BNScriptPubKey.h
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNObject.h"

@interface BNScriptPubKey : BNObject

- (BOOL)isMultisig;

@end
