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
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"error",
                                                   @"inputs",
                                                   @"outputs",
                                                   @"hash",
                                                   @"isLocked",
                                                   nil]];
    return self;
}

- (id)sendToServer:(NSString *)message withArg:(id)arg
{
    id result = [_wallet.server sendMessage:message withObject:self withArg:arg];
    self.error = _wallet.server.error;
    return result;
}

- (void)fillForValue:(long long)value
{
    [self copySlotsFrom:[self sendToServer:@"fillForValue" withArg:[NSNumber numberWithLongLong:value]]];
}

- (void)subtractFee
{
    BNTx *tx = [self.wallet.server sendMessage:@"subtractFee" withObject:self];
    self.inputs = tx.inputs;
    self.outputs = tx.outputs;
    self.hash = tx.hash;
}

- (void)sign
{
    BNTx *tx = [self.wallet.server sendMessage:@"sign" withObject:self];
    self.inputs = tx.inputs;
    self.outputs = tx.outputs;
    self.hash = tx.hash;
}

- (void)addInputsFromTx:(BNEscrowTx *)tx
{
    
}

- (void)mergeWithTx:(BNEscrowTx *)tx
{
    
}

- (void)broadcast
{
    [self.wallet.server sendMessage:@"broadcast" withObject:self];
}

- (void)ping
{
    [self.wallet.server sendMessage:@"ping" withObject:self];
}

@end
