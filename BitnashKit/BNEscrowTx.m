//
//  BNEscrowTransaction.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNEscrowTx.h"
#import "BNTxIn.h"
#import "BNTxOut.h"
#import "NSObject+BNJSON.h"
#import "BNWallet.h"

@implementation BNEscrowTx

+ (NSArray *)jsonProperties
{
    return [NSArray arrayWithObjects:
            @"inputs",
            @"outputs",
            @"value",
            @"fee", nil];
}

- (NSNumber *)fee
{
    return nil;
}

- (void)fill
{
    BNEscrowTx *tx = [_wallet.server sendMessage:@"fillEscrowTx" withObject:_value];
    self.inputs = tx.inputs;
    self.outputs = tx.outputs;
}

@end
