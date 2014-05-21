//
//  BNWallet.h
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNServer.h"
#import "BNObject.h"
#import "BNTx.h"
#import "BNKey.h"
#import "BNDepositKey.h"
#import "BNTransactionsNode.h"

@class BNDepositKey; //why?

@interface BNWallet : BNObject

//1. Get Balance (seller)
//2. Get Deposit Address (seller)
//3. Get Balance (seller)
//4. Get Escrow Transaction (inputs / outputs/ pubkey) (seller)
//5. Serialize TX and send to buyer (seller)
//6. Get Escrow Transaction (inputs / outputs/ pubkey) (buyer)
//7. Load Serialized TX from buyer (buyer)
//8. Merge TXs (buyer)
//9. Sign TX (buyer)
//10. Serialize TX and send to seller (buyer)
//11. Load Serialized TX from buyer (seller)
//12. Broadcast TX  (seller)
//13. Get Conf Count (buyer/seller)
//14. ...
//15. Get Release TX (buyer)
//15. GOTO 5 for Release TX

@property BNServer *server;
//Start a BNServer and set it on this wallet prior to use.

@property BNTransactionsNode *transactionsNode;


- (void)setPath:(NSString *)path;
//Set that path to the wallet directory

- (void)setCheckpointsPath:(NSString *)path;
//Set the path to the checkpoints file

- (NSError *) error;
//Error for last operation.

- (NSNumber *)balance;
//return the balance of the wallet in Satoshi.  Blocking call.

- (BNKey *)createKey;
//Create a new keypair for the wallet and return it;

- (BNTx *)newTx;

- (NSArray *)transactions;

- (NSArray *)keys;

- (BNDepositKey *)depositKey; //TODO: This might return address used in tx that hasn't been broadcasted yet.

- (NSString *)status;
//The current status of the server (initialized, starting, started)

- (BNTx *)newWithdrawalTxToAddress:(NSString *)address withValue:(NSNumber *)value;

- (BOOL)isRunning;

@end
