//
//  BNMultisigScriptPubKey.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNMultisigScriptPubKey.h"

@implementation BNMultisigScriptPubKey

+ (NSArray *)jsonProperties
{
    return [NSArray arrayWithObjects:@"pubKeys", nil];
}

- (BOOL)isMultisig
{
    return YES;
}

@end
