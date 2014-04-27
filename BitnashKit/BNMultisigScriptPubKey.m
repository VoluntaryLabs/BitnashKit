//
//  BNMultisigScriptPubKey.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNMultisigScriptPubKey.h"

@implementation BNMultisigScriptPubKey

- (BOOL)isMultisig
{
    return YES;
}

- (id)init
{
    self = [super init];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"pubKeys",
                                                   nil]];
    self.pubKeys = [NSMutableArray array];
    return self;
}

@end
