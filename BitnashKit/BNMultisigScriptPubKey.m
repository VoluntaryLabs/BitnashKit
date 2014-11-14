//
//  BNMultisigScriptPubKey.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
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

- (BOOL)isEqual:(id)object
{
    BNMultisigScriptPubKey *other = (BNMultisigScriptPubKey *)object;
    
    return [object isKindOfClass:[BNMultisigScriptPubKey class]] &&
        [self.pubKeys isEqualToArray:other.pubKeys];
}

@end
