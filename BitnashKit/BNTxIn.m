//
//  BNTxIn.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import "BNTxIn.h"
#import "BNTxOut.h"
#import "BNTx.h"

@implementation BNTxIn

- (id)init
{
    self = [super init];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"scriptSig",
                                                   @"previousTxSerializedHex",
                                                   @"previousTxHash",
                                                   @"previousOutIndex",
                                                   nil]];
    return self;
}

- (void)configureFromTxOut:(BNTxOut *)txOut
{
    self.previousTxHash = txOut.parentTx.txHash;
    self.previousOutIndex = [txOut index];
}

- (BOOL)isEqual:(id)object
{
    BNTxIn *other = (BNTxIn *)object;
    
    return [object isKindOfClass:[BNTxIn class]] &&
        [self.previousTxHash isEqual:other.previousTxHash] &&
        [self.previousOutIndex isEqual:other.previousOutIndex];
}

@end
