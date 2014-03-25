//
//  BNTx.m
//  BitnashKit
//
//  Created by Rich Collins on 3/24/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNTx.h"
#import "BNWallet.h"

@implementation BNTx

- (id)init
{
    self = [super init];
    self.isLocked = [NSNumber numberWithBool:NO];
    self.inputs = [NSMutableArray array];
    self.outputs = [NSMutableArray array];
    return self;
}

- (void)fillForValue:(long long)value
{
    
}

- (void)addFee
{
    BNTx *tx = [self.wallet.server sendMessage:@"addFeeToTx" withObject:self];
    self.inputs = tx.inputs;
    self.outputs = tx.outputs;
    self.hash = tx.hash;
}

- (void)sign
{
    
}

- (void)addInputsFromTx:(BNEscrowTx *)tx
{
    
}

- (void)mergeWithTx:(BNEscrowTx *)tx
{
    
}

- (void)broadcast
{
    
}

@end
