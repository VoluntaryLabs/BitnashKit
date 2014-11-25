//
//  BNTxOut.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import "BNTxOut.h"
#import "BNTx.h"

@implementation BNTxOut

- (id)init
{
    self = [super init];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"value",
                                                   @"scriptPubKey",
                                                   nil]];
    return self;
}

- (NSNumber *)index
{
    return [NSNumber numberWithInteger:[[self parentTx].outputs indexOfObject:self]];
}

- (BNTx *)parentTx
{
    return (BNTx *)self.bnParent;
}

- (BOOL)isEqual:(id)object
{
    BNTxOut *other = (BNTxOut *)object;
    
    return [object isKindOfClass:[BNTxOut class]] &&
        [self.value isEqual:other.value] &&
        [self.scriptPubKey isEqual:other.scriptPubKey];
}

@end
