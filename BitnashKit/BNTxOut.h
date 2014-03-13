//
//  BNTxOut.h
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNScriptPubKey.h"

@interface BNTxOut : NSObject

@property NSNumber *value;
@property BNScriptPubKey *scriptPubKey;

@end
