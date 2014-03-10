//
//  BNEscrowTransaction.h
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>

@class BNWallet;

@interface BNEscrowTx : NSObject

@property (weak) BNWallet *wallet;

@property NSNumber *value;
//Value to be locked in escrow via multisig output.  Denominated in satoshi.

@property NSMutableArray *inputs;
@property NSMutableArray *outputs;

- (void)fill;
//fills the Transaction with inputs, outputs and a public key based on the transactions value

- (NSNumber *)fee;
//Fee associated with this tx.  Denominated in satoshi.

@end
